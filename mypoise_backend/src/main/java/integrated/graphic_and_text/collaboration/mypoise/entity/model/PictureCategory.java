package integrated.graphic_and_text.collaboration.mypoise.entity.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图片类别表
 * @TableName picture_category
 */
@TableName(value ="picture_category")
@Data
public class PictureCategory implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类型使用数
     */
    private Integer applyTotal;

    /**
     * 类别名称
     */
    private String categoryName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}