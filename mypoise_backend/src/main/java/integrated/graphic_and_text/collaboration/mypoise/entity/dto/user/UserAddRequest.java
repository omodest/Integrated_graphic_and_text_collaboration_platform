package integrated.graphic_and_text.collaboration.mypoise.entity.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 添加用户请求
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户简介
     */
    private String userProfile;

    private String userRole;

    /**
     * 会员过期时间
     */
    private Date vip_expire;

    private static final long serialVersionUID = 1L;
}
