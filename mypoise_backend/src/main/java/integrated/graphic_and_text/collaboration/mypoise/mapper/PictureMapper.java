package integrated.graphic_and_text.collaboration.mypoise.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author poise
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-01-17 16:25:07
* @Entity integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {
    @Select("SELECT " +
            "    pc.categoryName AS categoryName, " +
            "    COUNT(p.id) AS count, " +
            "    SUM(p.picSize) AS totalSize " +
            "FROM " +
            "    picture p " +
            "LEFT JOIN " +
            "    picture_category pc " +
            "ON " +
            "    p.categoryId = pc.id " +
            "    ${ew.customSqlSegment} " +  // 动态条件, 这个动态条件queryWrapper中自带了一个where子句，这里千万别在动态条件前加WHERE
            "GROUP BY " +
            "    categoryName")
    List<Map<String, Object>> getSpaceCategoryAnalyze(@Param("ew") QueryWrapper<Picture> queryWrapper);



     @Select("SELECT " +
             "    pt.tagName AS tagName, " +
             "    COUNT(ptr.pictureId) AS usageCount " +
             "FROM " +
             "    picture_tag_relation ptr " +
             "LEFT JOIN " +
             "    picture_tags pt " +
             "ON " +
             "    ptr.tagId = pt.id " +
             "LEFT JOIN picture p" +
             " ON "+
             " ptr.pictureId = p.id " +
             "${ew.customSqlSegment} " +  // 动态条件, 这个动态条件queryWrapper中自带了一个where子句，这里千万别在动态条件前加WHERE
             "GROUP BY " +
             "    ptr.pictureId " +
             "ORDER BY " +
             "    usageCount DESC")
     List<Map<String, Object>> getSpaceTagAnalyze(@Param("ew") QueryWrapper<Picture> queryWrapper);

}




