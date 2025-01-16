package integrated.graphic_and_text.collaboration.mypoise.entity.dto.user;

import lombok.Data;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserUpdateRequest implements Serializable {

    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    private String userRole;
    /**
     * 会员过期时间
     */
    private Date vip_expire;

    private static final long serialVersionUID = 1L;
}

