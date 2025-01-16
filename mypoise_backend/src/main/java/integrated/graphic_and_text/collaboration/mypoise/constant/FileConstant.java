package integrated.graphic_and_text.collaboration.mypoise.constant;

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
     * 上传头像的文件上传大小(1MB)
     */
    final long FILE_SIZE_UPLOAD_LIMIT = 1024 * 1024L;

    /**
     * 上传投降的文件类型
     */
    String [] FILE_TYPE_UPLOAD_LIMIT = {"jpeg", "jpg", "svg", "png", "webp"};
}
