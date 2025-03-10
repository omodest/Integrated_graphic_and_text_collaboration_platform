package integrated.graphic_and_text.collaboration.mypoise.entity.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱登录请求
 */
@Data
public class UserEmailLoginRequest implements Serializable {

    /**
     * 用户qq邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String captcha;


    private static final long serialVersionUID = 1L;
}
