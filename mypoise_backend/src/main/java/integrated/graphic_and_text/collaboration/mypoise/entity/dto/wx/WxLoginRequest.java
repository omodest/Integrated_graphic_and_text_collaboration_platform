package integrated.graphic_and_text.collaboration.mypoise.entity.dto.wx;

import lombok.Data;

/**
 * 小程序登录请求
 */
@Data
public class WxLoginRequest {

    /**
     * 小程序端登录传来的code
     */
    private String code;


    /**
     * 小程序登录 传来的用户信息
     */
    private WxUserInfo userInfo;
}
