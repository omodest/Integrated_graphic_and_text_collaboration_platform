package integrated.graphic_and_text.collaboration.mypoise.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureUploadRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadPictureResult;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
* @author poise
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-01-17 16:25:07
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片（数据万象）
     * @param multipartFile 上传的文件
     * @param uploadPathPrefix 上传路径的前缀
     * @return
     */
    UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix);

    /**
     * 文件校验（上传）
     * @param multipartFile 上传的文件
     */
    void validPicture(MultipartFile multipartFile);

    /**
     * 文件校验
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 删除临时文件
     * @param file 文件
     */
    void deleteTempFile(File file);

    /**
     * 文件上传（对象存储）
     * @param multipartFile 上传的文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 拼接查询 query_wrapper
     * @param pictureQueryRequest 查询请求
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片脱敏信息(返回给前端的服务)
     * @param picture 单张图片
     * @return
     */
    PictureVO getPictureVo(Picture picture);

    /**
     * 获取图片脱敏信息(返回给前端的服务)
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPagePictureVo(Page<Picture> picturePage);
}
