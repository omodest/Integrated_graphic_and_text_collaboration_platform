package integrated.graphic_and_text.collaboration.mypoise.entity.dto.wx;

import lombok.Data;

/**
 * 微信接口响应封装
 */
@Data
public class WxSessionResponse {

    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 会话密钥
     */
    private String session_key;

    /**
     * 联合ID（可选）
     */
    private String unionid;

    /**
     * 错误码
     */
    private String errcode;

    /**
     * 错误信息
     */
    private String errmsg;
}
