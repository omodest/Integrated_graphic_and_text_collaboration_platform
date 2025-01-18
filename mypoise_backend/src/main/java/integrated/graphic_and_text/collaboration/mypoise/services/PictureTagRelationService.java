package integrated.graphic_and_text.collaboration.mypoise.services;

import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureTagRelation;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author poise
* @description 针对表【picture_tag_relation(图片与标签关联表)】的数据库操作Service
* @createDate 2025-01-17 16:25:07
*/
public interface PictureTagRelationService extends IService<PictureTagRelation> {

    /**
     * 处理 图片标签之前的关系
     * @param tagNames
     */
    void handelTags(List<String> tagNames, long pictureId, HttpServletRequest httpServletRequest);

    /**
     * 获取标签集合
     * @param PictureId
     * @return
     */
    List<String> getListTagNameWithPicId(long PictureId);
}
