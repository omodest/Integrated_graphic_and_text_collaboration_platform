package integrated.graphic_and_text.collaboration.mypoise.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件管理常量(腾讯云COS)
 *
 * 接口中的变量声明，默认是 private static final
 */
public interface FileConstant {

    /**
     * 腾讯COS 访问地址
     */
    String COS_HOST = "https://mypoise-1309184980.cos.ap-guangzhou.myqcloud.com";

    /**
     * 上传头像的文件上传大小(2MB)
     */
    long FILE_SIZE_UPLOAD_LIMIT = 1024 * 1024L;

    /**
     * 上传图片的文件类型
     */
    List<String> FILE_TYPE_UPLOAD_LIMIT = Arrays.asList("jpeg", "jpg", "svg", "png", "webp");

}
