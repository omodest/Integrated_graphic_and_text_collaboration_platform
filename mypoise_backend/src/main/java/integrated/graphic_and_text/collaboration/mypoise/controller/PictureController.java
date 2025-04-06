package integrated.graphic_and_text.collaboration.mypoise.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.api.aliyunai.AliYunAiApi;
import integrated.graphic_and_text.collaboration.mypoise.api.aliyunai.model.CreateOutPaintingTaskResponse;
import integrated.graphic_and_text.collaboration.mypoise.api.aliyunai.model.GetOutPaintingTaskResponse;
import integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.ImageSearchApiFacade;
import integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.model.ImageSearchResult;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.DeleteRequest;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.PictureReviewStatusEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.SpaceUserAuthManager;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.StpKit;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.annotation.SaSpaceCheckPermission;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.model.SpaceUserAuthConfig;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.model.SpaceUserPermissionConstant;
import integrated.graphic_and_text.collaboration.mypoise.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static integrated.graphic_and_text.collaboration.mypoise.constant.CacheConstant.LOCAL_CACHE;

/**
 * 图片控制器
 */
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
    private SpaceService spaceService;

    /**
     * Redis缓存
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 阿里AI
     */
    @Resource
    private AliYunAiApi aliYunAiApi;

    /**
     * 空间用户权限
     */
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;


    /**
     * 删除图片 同步删除标签记录表中数据
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getCurrentUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 处理标签
        List<String> tagIds = pictureUpdateRequest.getTags();
        pictureTagRelationService.handelTags(tagIds, pictureUpdateRequest.getId(), httpServletRequest);
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
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.editPicture(pictureEditRequest, request);
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
        // 获取标签名，便于后续渲染数据
        List<String> listTagNameWithPicId = pictureTagRelationService.getListTagNameWithPicId(id);
        picture.setTagNames(listTagNameWithPicId);
        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "获取图片标签过程出错");
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类--查看图片详情）
     * 这个不登陆用户也能查看，所以不能加权限(因为加了sa-token强制需要用户登录，不登陆就会出错)
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验，space 为null表示公共空间
        Long spaceId = picture.getSpaceId();
        Space space = null;
        if (spaceId != null) {
            // 非公共空间至少要有浏览者权限
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 给返回的VO类绑定其对应的标签
        PictureVO pictureVo = pictureService.getPictureVo(picture);
        List<String> listTagNameWithPicId = pictureTagRelationService.getListTagNameWithPicId(id);
        pictureVo.setTagNames(listTagNameWithPicId);
        // 给返回的VO类绑定其对应的分类
        QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",picture.getCategoryId());
        pictureVo.setCategory(pictureCategoryService.getOne(queryWrapper).getCategoryName());
        // 获取权限列表（获取当前用户所拥有的权限）
        User loginUser = userService.getCurrentUser(httpServletRequest);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        pictureVo.setPermissionList(permissionList);
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
     * 这个不登陆用户也能查看，所以不能加权限(因为加了sa-token强制需要用户登录，不登陆就会出错)
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 空间权限校验，没有spaceID表示公共图库
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 普通用户默认只能看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else if (StrUtil.isNotEmpty(pictureQueryRequest.getName()) && pictureQueryRequest.getName().equals("小程序直通参数")){
                pictureQueryRequest.setName(null);
        }else {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
        }
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
        // 将分页图片转为Vo对象
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

    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 空间权限校验，没有spaceID表示公共图库
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 普通用户默认只能看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
        }

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
        // 为每个图片对象添加标签
        for (Picture picture : picturePage.getRecords()) {
            List<String> listTagNameWithPicId = pictureTagRelationService.getListTagNameWithPicId(picture.getId());
            picture.setTagNames(listTagNameWithPicId);
            QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",picture.getCategoryId());
            picture.setCategory(pictureCategoryService.getOne(queryWrapper).getCategoryName());
        }
        // 将分页图片转为Vo对象
        Page<PictureVO> pagePictureVo = null;
        if (ObjectUtil.isNotEmpty(picturePage.getRecords())){
            pagePictureVo  = pictureService.getPagePictureVo(picturePage);
        }
        if (picturePage.getRecords().isEmpty()){
            return ResultUtils.success(pagePictureVo);
        }
        String cacheValue = JSONUtil.toJsonStr(pagePictureVo);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 5 +  RandomUtil.randomInt(0, 5);
        valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        // 返回结果
        return ResultUtils.success(pagePictureVo);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(oldPicture.getUrl());
        return ResultUtils.success(resultList);
    }

    /**
     * 以色搜图
     * @param searchPictureByColorRequest
     * @param request
     * @return
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getCurrentUser(request);
        List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getCurrentUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
                                                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest == null ||
                createPictureOutPaintingTaskRequest.getPictureId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getCurrentUser(request);
        // 创建AI任务
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }
    /**
     * 查询 AI 扩图任务(扩图任务成功提示用户)
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }

    /**
     * TODO  将公共图库图片复制到 自己私人空间或团队空间/ 将自己私人空间或团队空间 复制到 公共空间
     * @param picture
     * @param target
     * @return
     */
//    @PostMapping("/update/space")
//    public BaseResponse<Boolean> toOtherSpace(@RequestBody Picture picture, int target, HttpServletRequest request){
//        ThrowUtils.throwIf(picture == null, ErrorCode.OPERATION_ERROR);
//        // 获取必要的字段
//        Long spaceId = picture.getSpaceId();
//        User currentUser = userService.getCurrentUser(request);
//        // 公共图库 转私人或团队
//        if (ObjectUtil.isEmpty(spaceId)){
//            QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("userId", currentUser.getId());
//            queryWrapper.eq("spaceType", target);
//            // 找到需要存储到自己的私人 或者团队空间
//            Space myTargetSpace = spaceService.getOne(queryWrapper);
//            // 给图片表添加一条数据
//            Picture targetPicture = new Picture();
//            BeanUtils.copyProperties(picture, targetPicture);
//            targetPicture.setSpaceId(myTargetSpace.getId());
//            boolean save = pictureService.save(targetPicture);
//            return ResultUtils.success(save);
//        }else {
//            // 直接添加到公共图库
//            Picture targetPicture = new Picture();
//            BeanUtils.copyProperties(picture, targetPicture);
//            targetPicture.setSpaceId(null);
//            boolean save = pictureService.save(targetPicture);
//            return ResultUtils.success(save);
//        }
//    }


    /**
     * 将当前图片应用到头像
     * @param picture
     * @param request
     * @return
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<Boolean> applyAvatar(@RequestBody Picture picture, HttpServletRequest request){
        ThrowUtils.throwIf(picture == null, ErrorCode.OPERATION_ERROR);
        User currentUser = userService.getCurrentUser(request);
        currentUser.setUserAvatar(picture.getUrl());
        boolean save = userService.updateById(currentUser);
        return ResultUtils.success(save);
    }

}
