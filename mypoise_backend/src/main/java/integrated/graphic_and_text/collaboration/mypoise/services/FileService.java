package integrated.graphic_and_text.collaboration.mypoise.services;

import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadPictureByBatchRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.FileUploadBizEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface FileService {

    /**
     * 图片上传校验
     * @param multipartFile 上传的图片
     * @param fileUploadBizEnum 业务类型
     */
    void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum);

    /**
     * 构建文件上传目录
     * @param multipartFile 文件
     * @param fileUploadBizEnum 文件上传类型
     * @param request 获取当前用户
     * @return 构建的文件路径
     */
    String buildContent(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum, HttpServletRequest request);

    /**
     * 文件上传
     *
     * @param filepath      文件上传路径
     * @param multipartFile 上传的文件
     */
    void uploadFile(String filepath, MultipartFile multipartFile);

    /**
     * 批量抓取和创建图片
     *
     * @param uploadPictureByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            UploadPictureByBatchRequest uploadPictureByBatchRequest,
            User loginUser
    );

}
