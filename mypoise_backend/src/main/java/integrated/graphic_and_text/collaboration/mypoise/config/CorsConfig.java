package integrated.graphic_and_text.collaboration.mypoise.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        // 覆盖所有请求
        registry.addMapping("/**")
                .allowCredentials(true) // 允许发送cookie
                .allowedOriginPatterns("*") // 放行哪些域名(必须用 patterns，否则*会和 allowcredentials 冲突)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
