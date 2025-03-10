package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadPictureByBatchRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureUploadRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.FileUploadBizEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.manage.CosManager;
import integrated.graphic_and_text.collaboration.mypoise.services.FileService;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
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

    @Resource
    private PictureService pictureService;

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

    @Override
    public Integer uploadPictureByBatch(UploadPictureByBatchRequest uploadPictureByBatchRequest, User loginUser) {
        // 1. 参数处理
        Integer count = uploadPictureByBatchRequest.getCount();
        String searchText = uploadPictureByBatchRequest.getSearchText();
        ThrowUtils.throwIf(count > 30 || StrUtil.isEmpty(searchText), ErrorCode.PARAMS_ERROR);
        String namePrefix = uploadPictureByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }

        // 2. 抓取前的配置
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            // 3. 用dom对象拿到整个页面
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 4. 拿到bing中唯一的的dgControl控制器
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        // 5. 从控制器中拿到img.mimg选择器
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        // 6. 遍历所有的img选择器
        for (Element imgElement : imgElementList) {
            // 7. 拿到src后的图片路径
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 8. 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 9. 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            try {
                if (StrUtil.isNotBlank(namePrefix)) {
                    // 设置图片名称，序号连续递增
                    pictureUploadRequest.setPicName(namePrefix + (count + 1));
                }
                PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }
}