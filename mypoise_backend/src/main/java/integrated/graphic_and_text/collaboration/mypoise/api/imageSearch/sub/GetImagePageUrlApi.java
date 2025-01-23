package integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过用户上传的图片路径，获取到百度识图的URL路径。
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片页面地址
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 构建向百度发送请求的 JSON请求对象。
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        try {
            // 2. 发送 POST 请求到百度接口, 并拿到响应
            HttpResponse response = HttpRequest.post(url)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            // 判断响应状态
            ThrowUtils.throwIf(HttpStatus.HTTP_OK != response.getStatus(),
                    ErrorCode.OPERATION_ERROR, "接口调用失败" );
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);
            // 3. 处理响应结果
            ThrowUtils.throwIf(result == null || !Integer.valueOf(0).equals(result.get("status")),
                    ErrorCode.OPERATION_ERROR, "接口调用失败");
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            ThrowUtils.throwIf(searchResultUrl == null, ErrorCode.OPERATION_ERROR, "未返回有效结果");
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }
}
