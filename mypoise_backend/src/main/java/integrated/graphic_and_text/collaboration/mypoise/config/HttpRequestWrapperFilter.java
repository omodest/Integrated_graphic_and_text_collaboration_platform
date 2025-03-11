package integrated.graphic_and_text.collaboration.mypoise.config;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求包装过滤器
 *
 *  系统中通过Hutool的工具类 servletUtil从 HttpServletRequest中获取到了参数信息；
 *  但HttpServletRequest 的 body 值是个流，**只支持读取一次，读完就没了，我们系统中getAuthContextByRequest可能需要重复读取，所以：
 *  这里自定义请求包装类和请求包装类过滤器，缓存请求体内容，以便后续多次读取。
 * @author pine
 */
@Order(1) // 设置过滤器优先级
@Component
public class HttpRequestWrapperFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            String contentType = servletRequest.getHeader(Header.CONTENT_TYPE.getValue());
            // 判断是否为 JSON 请求
            if (ContentType.JSON.getValue().equals(contentType)) {
                // 包装请求对象
                chain.doFilter(new RequestWrapper(servletRequest), response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }
}