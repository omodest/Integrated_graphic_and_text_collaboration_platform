package integrated.graphic_and_text.collaboration.mypoise.entity.dto.user;

import lombok.Data;

@Data
public class UserLoginRequest {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;


    private static final long serialVersionUID = 1L;
}
