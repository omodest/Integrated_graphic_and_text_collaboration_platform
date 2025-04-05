//package integrated.graphic_and_text.collaboration.mypoise.utils;
//
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class HttpClientUtils {
//    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
//
//    public static String get(String url) throws IOException {
//        HttpGet httpGet = new HttpGet(url);
//        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
//            return EntityUtils.toString(response.getEntity());
//        }
//    }
//}
