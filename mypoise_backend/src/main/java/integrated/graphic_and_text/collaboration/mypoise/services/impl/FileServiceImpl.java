package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.FileUploadBizEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.manage.CosManager;
import integrated.graphic_and_text.collaboration.mypoise.services.FileService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

import static integrated.graphic_and_text.collaboration.mypoise.constant.FileConstant.FILE_SIZE_UPLOAD_LIMIT;
import static integrated.graphic_and_text.collaboration.mypoise.constant.FileConstant.FILE_TYPE_UPLOAD_LIMIT;

/**
 * 文件上传服务
 */
@Slf4j
@Service // 业务逻辑层注解，与表示层@Controller功能基本一样，区别就是名称，以及使用位置不同。
public class FileServiceImpl implements FileService {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Override
    public void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 校验
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > FILE_SIZE_UPLOAD_LIMIT) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList(FILE_TYPE_UPLOAD_LIMIT).contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    @Override
    public String buildContent(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum, HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        return String.format("/%s/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), DateUtil.formatDate(new Date()), filename);
    }

    @Override
    public void uploadFile(String filepath, MultipartFile multipartFile) {
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(filepath, null);
            // 将前端上传的文件，写到file临时文件中
            multipartFile.transferTo(file);
            // 上传到腾讯云cos
            cosManager.putObject(filepath, file);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }
}
