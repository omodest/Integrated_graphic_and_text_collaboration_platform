package integrated.graphic_and_text.collaboration.mypoise.config;

import integrated.graphic_and_text.collaboration.mypoise.entity.dto.wx.WxSessionResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 小程序关键配置
 */
@Data
@Configuration
public class WxConfig {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;


    @Value("${wechat.auth-url}")
    private String authUrl;

    @Value("${wechat.redirectUri}")
    private String redirectUri;

    public WxSessionResponse getWxSession(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                authUrl, appid, secret, code);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, WxSessionResponse.class);
    }
}
