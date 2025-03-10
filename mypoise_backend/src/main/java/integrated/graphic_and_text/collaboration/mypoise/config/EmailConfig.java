package integrated.graphic_and_text.collaboration.mypoise.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * QQ邮箱注册
 * 用来实现邮箱登录和注册
 */
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
@Data
public class EmailConfig {

    private String emailFrom;
}
