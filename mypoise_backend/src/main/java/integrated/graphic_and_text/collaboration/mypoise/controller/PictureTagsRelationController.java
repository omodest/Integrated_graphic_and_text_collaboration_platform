package integrated.graphic_and_text.collaboration.mypoise.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.*;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureService;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureTagRelationService;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureTagsService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static integrated.graphic_and_text.collaboration.mypoise.constant.CacheConstant.HOT_TAGS;

@RestController
@RequestMapping("/picture/tags")
@Slf4j
public class PictureTagsRelationController {

    @Resource
    private UserService userService;

    @Resource
    private PictureTagsService pictureTagsService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 删除标签
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/del")
    public BaseResponse<Boolean> delTags(@RequestBody long tagId, HttpServletRequest httpServletRequest){
        // 1. 参数校验
        ThrowUtils.throwIf(tagId <= 0, ErrorCode.PARAMS_ERROR);
        // 2. 找到要删除的标签
        PictureTags pictureTags = pictureTagsService.getById(tagId);
        ThrowUtils.throwIf(pictureTags == null, ErrorCode.NOT_FOUND_ERROR, "标签不存在");
        Long userId = pictureTags.getUserId();
        // 3. 权限校验
        User loginUser = userService.getCurrentUser(httpServletRequest);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser) && !(loginUser.getId().equals(userId)),
                ErrorCode.NOT_FOUND_ERROR, "仅本人和管理员可删除");
        // 4. 删除标签
        boolean b = pictureTagsService.removeById(pictureTags);
        // 5. 删除图片标签表记录
        QueryWrapper<PictureTagRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tagId",tagId);
//        boolean remove = pictureTagRelationService.remove(queryWrapper);
//        ThrowUtils.throwIf(!b || !remove, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询所有标签(按照使用频率排序返回)
     * @return
     */
    @PostMapping("/get")
    public BaseResponse<List<PictureTags>> queryTags(){
        QueryWrapper<PictureTags> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("applyTotal");
        // 查询热门分类
        List<PictureTags> pictureCategoryList = pictureTagsService.list(queryWrapper);
        return ResultUtils.success(pictureCategoryList);
    }

    /**
     * 热门标签
     * @return
     */
    @GetMapping("/hot")
    public BaseResponse<String> getHotTags() {
        // 查找缓存
        String getCacheHotTag = stringRedisTemplate.opsForValue().get(HOT_TAGS);
        // 未命中，查找数据库；写缓存
        List<PictureTags> list = null;
        if (StrUtil.isEmpty(getCacheHotTag)){
            list = pictureTagsService.query()
                    .orderByDesc("applyTotal")
                    .last("LIMIT 3")
                    .list();
            String topTagNames = list.stream().map(PictureTags::getTagName).collect(Collectors.toList()).toString();
            stringRedisTemplate.opsForValue().set(HOT_TAGS, topTagNames, 60, TimeUnit.SECONDS);
        }
        return ResultUtils.success(getCacheHotTag);
    }
}
