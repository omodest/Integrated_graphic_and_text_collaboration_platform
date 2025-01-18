package integrated.graphic_and_text.collaboration.mypoise.entity.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Data
@TableName(value = "picture_tag_relation")
public class PictureTagRelation implements Serializable {


    private Long pictureId;

    private Long tagId;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
