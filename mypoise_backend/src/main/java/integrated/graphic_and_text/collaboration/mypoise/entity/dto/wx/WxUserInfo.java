package integrated.graphic_and_text.collaboration.mypoise.entity.dto.wx;

import lombok.Data;

/**
 * 拿到微信用户信息
 */
@Data
public class WxUserInfo {

    private String openid;


    private String unionid;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private String gender;

    /**
     * 城市
     */
    private String city;
}
