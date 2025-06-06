package integrated.graphic_and_text.collaboration.mypoise.entity.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 使用邮箱修改密码
 */
@Data
public class UserEmailEditPwdRequest implements Serializable {

    /**
     * 用户qq邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String captcha;

    private String userPassword;


    private static final long serialVersionUID = 1L;
}
