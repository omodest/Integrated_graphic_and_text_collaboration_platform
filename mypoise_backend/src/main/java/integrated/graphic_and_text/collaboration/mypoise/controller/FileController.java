package integrated.graphic_and_text.collaboration.mypoise.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.constant.FileConstant;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureUploadRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadFileRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.FileUploadBizEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.manage.CosManager;
import integrated.graphic_and_text.collaboration.mypoise.services.FileService;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
//  region 注解讲解
// 1. SLF4J 是一个简单的日志门面，它提供了一个日志抽象层，简化日志对象的使用。SLF4J 本身不输出日志，而是与不同的日志实现（如 Logback）配合，支持日志的持久化存储和控制台输出。
// 默认情况下@Slf4j 只输出到控制台
// 2. @Controller 注解主要用于标识一个控制器，通常用于处理 Web 请求，返回视图名,
// Spring Boot 中，如果使用 @Controller 返回一个字符串，Spring 会将其视为视图名，尝试解析并返回相应的视图。
//  @ResponseBody 注解用于指示方法的返回值直接写入 HTTP 响应体，适用于希望返回 JSON 或 XML 数据的场景，避免将返回值视为视图名。
// 使用 @RestController 注解组合了 @Controller 和 @ResponseBody 的功能，通过将 @ResponseBody 的行为提取到类上，确保该控制器的所有方法都是以 RESTful 的方式返回数据。这种一致性有助于维护和扩展代码。
// 3. RequestMapping 用于映射 HTTP请求的url 到处理方法的注解。它可以用于类级别和方法级别，提供了灵活的请求处理功能。
// endregion
public class FileController {

    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    private final FileService fileService;

    @Autowired // 从 Spring 4.3 开始，如果一个类只有一个构造函数，Spring 会自动使用该构造函数进行依赖注入，因此可以省略 @Autowired 注解。
    public FileController(FileService fileService){
        this.fileService = fileService;
    }


    /**
     * 头像上传
     *
     * @param multipartFile 上传的文件
     * @param uploadFileRequest 上传文件类型
     * @param request http请求
     * @return 文件上传可访问地址
     */
    @PostMapping("/upload") // 相当于 @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           // 文件上传时，使用@RequestPart("file") 注解用于从 multipart 请求中提取名为 "file" 的部分;
                                           // "file" 是用来匹配前端表单中上传文件的字段名称，以便在后端能够正确接收和处理该文件
                                           // @RequestPart：用于处理 multipart/form-data 请求中的某个部分，通常与文件上传相关;处理文件上传和表单数据，适合于需要传递多个不同类型数据的场景
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        // 1. 拿到上传文件的类型
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件上传类型为空");
        }
        // 2. 文件校验
        fileService.validFile(multipartFile, fileUploadBizEnum);
        // 3. 文件目录：根据业务、用户来划分
        String filepath = fileService.buildContent(multipartFile, fileUploadBizEnum, request);
        // 4. 上传文件
        fileService.uploadFile(filepath, multipartFile);
        // 返回可访问地址
        return ResultUtils.success(FileConstant.COS_HOST + filepath);
    }


    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

    /**
     * 平台核心功能- 图片上传
     * @param multipartFile 文件
     * @param pictureUploadRequest 图片上传请求
     * @param httpServletRequest http上下文
     * @return
     */
    @PostMapping("/upload/picture")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest httpServletRequest){
        User currentUser = userService.getCurrentUser(httpServletRequest);
        // 文件上传
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, currentUser);
        return ResultUtils.success(pictureVO);
    }

}
