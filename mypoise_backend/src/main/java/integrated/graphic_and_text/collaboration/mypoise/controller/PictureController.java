package integrated.graphic_and_text.collaboration.mypoise.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.DeleteRequest;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.PictureReviewStatusEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureCategory;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureTagRelation;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureCategoryService;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureService;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureTagRelationService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static integrated.graphic_and_text.collaboration.mypoise.constant.CacheConstant.LOCAL_CACHE;


@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private PictureTagRelationService pictureTagRelationService;

    @Resource
    private PictureCategoryService pictureCategoryService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 删除图片 同步删除标签记录表中数据
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 删除图片标签表的记录
        QueryWrapper<PictureTagRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId",deleteRequest.getId());
        boolean remove = pictureTagRelationService.remove(queryWrapper);
        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest httpServletRequest) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 处理标签
        List<String> tagIds = pictureUpdateRequest.getTags();
        pictureTagRelationService.handelTags(tagIds, pictureUpdateRequest.getId(), httpServletRequest);
//        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 管理员自动过审
        pictureService.filterReviewParam(picture,userService.getCurrentUser(httpServletRequest));
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);

        QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("categoryName", pictureEditRequest.getCategory());
        Long id1 = pictureCategoryService.getOne(queryWrapper).getId();
        if (ObjectUtil.isNotEmpty(id1)){
            picture.setCategoryId(id1);
        }else {
            picture.setCategoryId(1L);
        }


        // 处理标签
        List<String> tagIds = pictureEditRequest.getTags();
        pictureTagRelationService.handelTags(tagIds, pictureEditRequest.getId(), request);
//        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getCurrentUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 管理员自动过审
        pictureService.filterReviewParam(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取标签
        List<String> listTagNameWithPicId = pictureTagRelationService.getListTagNameWithPicId(id);
        picture.setTagNames(listTagNameWithPicId);
        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "获取图片标签过程出错");
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        // 获取标签
        List<String> listTagNameWithPicId = pictureTagRelationService.getListTagNameWithPicId(id);
        picture.setTagNames(listTagNameWithPicId);
        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "获取图片标签过程出错");
        // 获取封装类
        PictureVO pictureVo = pictureService.getPictureVo(picture);
        QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",picture.getCategoryId());
        pictureVo.setCategory(pictureCategoryService.getOne(queryWrapper).getCategoryName());
        return ResultUtils.success(pictureVo);
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 设置标签

        // 为每个图片对象添加标签
        for (Picture picture : picturePage.getRecords()) {
            List<String> listTagNameWithPicId = pictureTagRelationService.getListTagNameWithPicId(picture.getId());
            picture.setTagNames(listTagNameWithPicId);

            QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",picture.getCategoryId());
            picture.setCategory(pictureCategoryService.getOne(queryWrapper).getCategoryName());
        }
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));

        // 为每个图片对象添加标签
        for (Picture picture : picturePage.getRecords()) {
            List<String> listTagNameWithPicId = pictureTagRelationService.getListTagNameWithPicId(picture.getId());
            picture.setTagNames(listTagNameWithPicId);

            QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",picture.getCategoryId());
            picture.setCategory(pictureCategoryService.getOne(queryWrapper).getCategoryName());
        }

        Page<PictureVO> pagePictureVo = null;
        if (ObjectUtil.isNotEmpty(picturePage.getRecords())){
            pagePictureVo  = pictureService.getPagePictureVo(picturePage);
        }
        // 获取封装类
        return ResultUtils.success(pagePictureVo);
    }

    /**
     * 管理员审核图片
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getCurrentUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 多级缓存方案

        // 1. 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "mypoise:listPictureVOByPage:" + hashKey;


        // 2. 从本地缓存中查询
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            // 如果缓存命中，返回结果
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 3. 本地缓存未命中； 从 Redis 缓存中查询
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null) {
            // 如果缓存命中，返回结果；将数据存到本地缓存中
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 4. redis未命中，查询数据库，存redis、存本地
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPagePictureVo(picturePage);

        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 300 +  RandomUtil.randomInt(0, 300);
        valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        // 返回结果
        return ResultUtils.success(pictureVOPage);
    }



}
