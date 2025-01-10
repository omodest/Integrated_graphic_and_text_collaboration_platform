package integrated.graphic_and_text.collaboration.mypoise.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户-微信表
 * @TableName user_wechat
 */
@TableName(value ="user_wechat")
@Data
public class UserWechat implements Serializable {
    /**
     * 用户微信表ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;

    /**
     * 微信扫码登录的OpenID
     */
    private String openid;

    /**
     * 微信扫码登录的SessionKey
     */
    private String session_key;

    /**
     * 微信统一标识 UnionID
     */
    private String unionid;

    /**
     * 微信小程序的OpenID
     */
    private String mini_program_openid;

    /**
     * 微信小程序的UnionID
     */
    private String mini_program_unionid;

    /**
     * 微信小程序绑定时间
     */
    private Date mini_program_bind_time;

    /**
     * 创建时间
     */
    private Date created_at;

    /**
     * 更新时间
     */
    private Date updated_at;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}