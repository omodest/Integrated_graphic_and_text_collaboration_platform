package integrated.graphic_and_text.collaboration.mypoise.manage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import integrated.graphic_and_text.collaboration.mypoise.config.CosClientConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;

/**
 * Cos 对象存储操作
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key 上传到云存储后的文件名或对象键，用于在云存储中唯一标识该文件。
     * @param localFilePath 本地文件路径
     * @return 表示上传操作的结果，通常包含上传的文件信息和状态。
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        // 上传文件的请求,
        // cosClientConfig.getBucket()：获取云存储桶的名称。桶是云存储中存储对象的容器，类似于文件夹。
        //key：之前定义的对象键，表示在桶中存储时的文件名。
        //new File(localFilePath)：将本地文件路径转换为 File 对象，以便于上传。
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        // 调用云存储客户端的 putObject 方法，执行文件上传
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param file 文件
     * @return 表示上传操作的结果，通常包含上传的文件信息和状态。
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        /**
         * 上传文件到对象存储，
         */
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);

        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

}
