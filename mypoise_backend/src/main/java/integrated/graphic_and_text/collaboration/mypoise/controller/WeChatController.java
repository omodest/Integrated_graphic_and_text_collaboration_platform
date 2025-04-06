package integrated.graphic_and_text.collaboration.mypoise.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.config.WxConfig;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadPictureResult;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureUploadRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.wx.WxUserInfo;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Space;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.annotation.SaSpaceCheckPermission;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.model.SpaceUserPermissionConstant;
import integrated.graphic_and_text.collaboration.mypoise.manage.upload.FilePictureUpload;
import integrated.graphic_and_text.collaboration.mypoise.manage.upload.PictureUploadTemplate;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureService;
import integrated.graphic_and_text.collaboration.mypoise.services.SpaceService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserWechatService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static integrated.graphic_and_text.collaboration.mypoise.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant.EMAIL_PATTERN;
import static integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant.USER_LOGIN_STATE;
import static integrated.graphic_and_text.collaboration.mypoise.services.impl.PictureServiceImpl.getPicture;

@RestController
@RequestMapping("/wechat")
@Slf4j
public class WeChatController {

//    @Resource
//    private UserWechatService weChatService;
//
//    @Resource
//    private WxConfig wxConfig;
//
//    @Resource
//    private UserService userService;
//
//    // 发起微信绑定
//    @GetMapping("/bind")
//    public void bindWeChat(HttpServletResponse response) throws IOException {
//        String authUrl = weChatService.getAuthorizationUrl();
//        response.sendRedirect(authUrl);
//    }
//
//    // 微信回调处理
//    @GetMapping("/callback")
//    public ResponseEntity<?> callback(@RequestParam String code,
//                                      HttpServletRequest request) {
//        try {
//            // 1. 获取微信用户信息
//            WxUserInfo wxUser = weChatService.getWxUserInfo(code);
//
//            // 2. 获取当前系统用户（需要根据你的认证系统实现）
//            User currentUser = userService.getCurrentUser(request);
//
//            // 3. 绑定到当前用户
//            weChatService.bindWeChat(currentUser.getId(), wxUser.getOpenid(), wxUser.getUnionid());
//
//            return ResponseEntity.ok().body("success");
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("error");
//        }
//    }

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 小程序 图片上传
     * @param multipartFile
     * @param pictureUploadRequest： 包含两个参数 picName文件名称 spaceId空间Id
     * @return
     */
    @PostMapping("/upload/picture")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                  Long userId){
        // 1. 拿到用户
        User currentUser = userService.getById(userId);
        // 文件上传
        PictureVO pictureVO = this.uploadPicture(multipartFile, pictureUploadRequest, currentUser);
        return ResultUtils.success(pictureVO);
    }

    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未找到用户信息");
        // 新增，校验空间参数
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
        // 2. 判断是新增还是修改(小程序只提供新增+++++++)
        Long pictureId = null;
        if (pictureUploadRequest != null){
            pictureId = pictureUploadRequest.getId();
        }

        if (pictureId != null){
            // 校验是否存在数据
            boolean exists = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            if (!exists){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不存在");
            }

            Picture oldPicture = pictureService.getById(pictureId);
            // 校验空间是否一致
            // 没传 spaceId，则复用原有图片的 spaceId（这样也兼容了公共图库）
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了 spaceId，必须和原图片的空间 id 一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }

        // 3. 增加数据库记录
        // 按照用户id划分目录
        // 按照用户 id 划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            // 公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            // 空间
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        // 调用 数据万象的图片上传(没使用模板方法前的 图片上传；现在使用模板方法 这里注释掉)
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构建entity
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureUploadRequest, pictureId);
        // 管理员自动过审
        pictureService.filterReviewParam(picture, loginUser);

        // 开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 插入数据
            boolean result = pictureService.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
            // 如果 finalSpaceId 不为 null，更新空间的使用额度
            if (finalSpaceId != null) {
                // 更新空间的使用额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });
        return PictureVO.objToVo(picture);
    }
}


















