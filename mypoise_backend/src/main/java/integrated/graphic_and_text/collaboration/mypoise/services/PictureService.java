package integrated.graphic_and_text.collaboration.mypoise.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadPictureResult;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
* @author poise
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-01-17 16:25:07
*/
public interface PictureService extends IService<Picture> {

//    /**
//     * 上传图片（数据万象）
//     * @param multipartFile 上传的文件
//     * @param uploadPathPrefix 上传路径的前缀
//     * @return
//     */
//    UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix);
//
//    /**
//     * 上传图片(URL上传)
//     * @return
//     */
//    UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix);
//
//    /**
//     * 文件校验（上传）
//     * @param multipartFile 上传的文件
//     */
//    void validPicture(MultipartFile multipartFile);
//
//    /**
//     * URL校验
//     * @param fileUrl
//     */
//    void validPicture(String fileUrl);

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
     * @param inputSource 文件上传的源信息
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

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
     * @return
     */
    Page<PictureVO> getPagePictureVo(Page<Picture> picturePage);

    /**
     * 图片审核
     * @param pictureReviewRequest  审核请求
     * @param loginUser 当前登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 管理员自动过审
     * @param picture
     * @param loginUser
     */
    void filterReviewParam(Picture picture, User loginUser);

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);
    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param request
     */
    void editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request);
    /**
     * 校验空间图片的权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 颜色搜索
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

}
