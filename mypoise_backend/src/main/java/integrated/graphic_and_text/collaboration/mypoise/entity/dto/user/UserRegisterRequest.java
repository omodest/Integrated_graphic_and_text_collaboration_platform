package integrated.graphic_and_text.collaboration.mypoise.entity.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 */
@Data
public class UserRegisterRequest implements Serializable {


    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;


    /**
     * 用户qq邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String captcha;

}
