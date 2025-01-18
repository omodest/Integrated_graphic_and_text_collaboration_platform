package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureTagRelation;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureTags;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureTagRelationService;
import integrated.graphic_and_text.collaboration.mypoise.mapper.PictureTagRelationMapper;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureTagsService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* @author poise
* @description 针对表【picture_tag_relation(图片与标签关联表)】的数据库操作Service实现
* @createDate 2025-01-17 16:25:07
*/
@Service
public class PictureTagRelationServiceImpl extends ServiceImpl<PictureTagRelationMapper, PictureTagRelation>
    implements PictureTagRelationService{

    @Resource
    private UserService userService;

    @Resource
    private PictureTagsService pictureTagsService;

    @Override
    public void handelTags(List<String> tagNames, long pictureId, HttpServletRequest httpServletRequest) {
        // 1. 参数校验
        if (tagNames == null || tagNames.size() < 0){
           return;
        }
        // 2. 拿到标签表里所有标签； 判断是否需要新增标签
        List<PictureTags> pictureTagsList = pictureTagsService.list();
        List<String> pictureTagNamesList = pictureTagsList.stream().map(PictureTags::getTagName)
                .collect(Collectors.toList());

        boolean hasDifference = tagNames.stream()
                .anyMatch(item -> !pictureTagNamesList.contains(item));
        // 3. 如果有标签不在标签表里,说明需要新增。新增(仅管理员和vip可以自定义标签)
        User currentUser = null;
        Long userId = null;
        if (hasDifference){
            currentUser = userService.getCurrentUser(httpServletRequest);
            userId = currentUser.getId();
            if (currentUser.getUserRole().equals(UserConstant.USER_ROLE) && !userService.isAdmin(currentUser)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "普通用户不允许自定义标签");
            }
        }

        // 4. 添加数据库记录
        for (String tagName: tagNames){
            if (!pictureTagNamesList.contains(tagName)){
                PictureTags pictureTags = new PictureTags();
                pictureTags.setTagName(tagName);
                pictureTags.setUserId(userId);
                pictureTagsService.save(pictureTags);
            }
            QueryWrapper<PictureTags> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("tagName", tagName);
            PictureTags pictureTags = pictureTagsService.getOne(queryWrapper);
            // 添加图片标签关联记录
            PictureTagRelation pictureTagRelation = new PictureTagRelation();
            pictureTagRelation.setPictureId(pictureId);
            pictureTagRelation.setTagId(pictureTags.getId());
            this.save(pictureTagRelation);
        }
    }

    @Override
    public List<String> getListTagNameWithPicId(long PictureId) {
        // 1. 拿到所有关联的记录
        QueryWrapper<PictureTagRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", PictureId);
        List<PictureTagRelation> pictureTagRelationList = this.list(queryWrapper);
        // 2. 拿到所有关联记录的 tagId
        List<Long> tagIds = pictureTagRelationList.stream().map(PictureTagRelation::getTagId).collect(Collectors.toList());
        // 3. tagId转tagName
        List<PictureTags> pictureTagsList = pictureTagsService.listByIds(tagIds);
        return pictureTagsList.stream().map(PictureTags::getTagName).collect(Collectors.toList());
    }
}




