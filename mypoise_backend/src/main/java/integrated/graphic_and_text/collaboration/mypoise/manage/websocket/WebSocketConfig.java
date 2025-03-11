package integrated.graphic_and_text.collaboration.mypoise.manage.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import javax.annotation.Resource;
/**
 * WebSocket 配置（定义连接）
 */
@Configuration
@EnableWebSocket // 开启WebSocket支持
public class WebSocketConfig implements WebSocketConfigurer {
    /**
     * 处理WebSocket消息的处理器
     */
    @Resource
    private PictureEditHandler pictureEditHandler;

    /**
     * WebSocket握手拦截器
     */
    @Resource
    private WsHandshakeInterceptor wsHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，并指定WebSocket的访问路径
        registry.addHandler(pictureEditHandler, "/ws/picture/edit")
                // 添加握手拦截器，可用于认证、日志记录等
                .addInterceptors(wsHandshakeInterceptor)
                // 允许所有来源的WebSocket连接（* 代表不做限制）
                .setAllowedOrigins("*");
    }
}