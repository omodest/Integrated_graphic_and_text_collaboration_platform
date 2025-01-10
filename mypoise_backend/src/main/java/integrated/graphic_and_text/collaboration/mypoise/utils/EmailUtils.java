package integrated.graphic_and_text.collaboration.mypoise.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import static integrated.graphic_and_text.collaboration.mypoise.constant.EmailConstant.*;

@Slf4j
public class EmailUtils {

    /**
     * 构建邮件内容
     */
    public static String buildEmailContent(String emailHtmlPath, String captcha){
        // 根据路径创建邮件模板
        ClassPathResource classPathResource = new ClassPathResource(emailHtmlPath);
        // 输入流
        InputStream inputStream = null;
        // 读字节
        BufferedReader bufferedReader = null;
        StringBuilder buffer = new StringBuilder();
        String line = "";
        try {
            inputStream = classPathResource.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            // 读取模板
            while ((line = bufferedReader.readLine()) != null){
                buffer.append(line);
            }
        }catch (Exception e){
            log.info("发送邮件读取模板失败{}", e.getMessage());
        }finally {
            try {
                if (inputStream != null){
                    inputStream.close();
                }
                if (bufferedReader != null){
                    bufferedReader.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        // 替换html模板中的参数
        return MessageFormat.format(buffer.toString(), captcha, EMAIL_TITLE, EMAIL_TITLE_ENGLISH, PLATFORM_RESPONSIBLE_PERSON, PLATFORM_ADDRESS);
    }





}
