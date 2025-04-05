package integrated.graphic_and_text.collaboration.mypoise.services.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.*;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import integrated.graphic_and_text.collaboration.mypoise.api.aliyunai.AliYunAiApi;
import integrated.graphic_and_text.collaboration.mypoise.api.aliyunai.model.CreateOutPaintingTaskRequest;
import integrated.graphic_and_text.collaboration.mypoise.api.aliyunai.model.CreateOutPaintingTaskResponse;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadPictureResult;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.PictureReviewStatusEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.manage.upload.FilePictureUpload;
import integrated.graphic_and_text.collaboration.mypoise.manage.upload.PictureUploadTemplate;
import integrated.graphic_and_text.collaboration.mypoise.manage.upload.UrlPictureUpload;
import integrated.graphic_and_text.collaboration.mypoise.mapper.PictureMapper;
import integrated.graphic_and_text.collaboration.mypoise.services.*;
import integrated.graphic_and_text.collaboration.mypoise.utils.ColorSimilarUtils;
import integrated.graphic_and_text.collaboration.mypoise.utils.ColorTransformUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author poise
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-01-17 16:25:07
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private UserService userService;

    @Resource
    private PictureCategoryService pictureCategoryService;

    @Resource
    private PictureTagsService pictureTagsService;

    @Resource
    private PictureTagRelationService pictureTagRelationService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;

//    @Override
//    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
//        // 1. 图片校验
//        validPicture(multipartFile);
//        // 2. 拼接存储路径
//        String uuid = RandomUtil.randomNumbers(16);
//        String originalFilename = multipartFile.getOriginalFilename();
//        String uploadPath = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
//        // 3. 上传图片
//        File tempFile = null;
//        try {
//            // 创建临时文件
//            tempFile = File.createTempFile(uploadPath, null);
//            multipartFile.transferTo(tempFile);
//            // 上传图片（数据万象）
//            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, tempFile);
//            // 拿到数据万向分析出来的图片信息
//            ImageInfo imageInfo = putObjectResult.getCiUploadResult()
//                    .getOriginalInfo()
//                    .getImageInfo();
//
//            // 封装返回结果
//            UploadPictureResult uploadPictureResult = new UploadPictureResult();
//            Integer width = imageInfo.getWidth();
//            Integer height = imageInfo.getHeight();
//            double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
//            String format = imageInfo.getFormat();
//            uploadPictureResult.setUrl(COS_HOST + "/" + uploadPath);
//            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
//            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
//            uploadPictureResult.setPicWidth(width);
//            uploadPictureResult.setPicHeight(height);
//            uploadPictureResult.setPicScale(picScale);
//            uploadPictureResult.setPicFormat(format);
//
//            return uploadPictureResult;
//        } catch (IOException e) {
//            log.error("数据上传到对象存储失败。(数据万象)");
//            // 处理文件上传异常
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
//        }finally {
//            // 删除临时文件
//            deleteTempFile(tempFile);
//        }
//    }
//
//    @Override
//    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
//        // 1. 图片校验
//        validPicture(fileUrl);
//        // 2. 拼接存储路径
//        String uuid = RandomUtil.randomString(16);
//        String originFilename = FileUtil.mainName(fileUrl);
//        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
//                FileUtil.getSuffix(originFilename));
//        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
//        File file = null;
//        try {
//            // 创建临时文件
//            file = File.createTempFile(uploadPath, null);
//            // 下根据URL载文件
//            HttpUtil.downloadFile(fileUrl, file);
//            // 上传图片
//             PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
//            // 拿到数据万向分析出来的图片信息
//            ImageInfo imageInfo = putObjectResult.getCiUploadResult()
//                    .getOriginalInfo()
//                    .getImageInfo();
//
//            // 封装返回结果
//            UploadPictureResult uploadPictureResult = new UploadPictureResult();
//            Integer width = imageInfo.getWidth();
//            Integer height = imageInfo.getHeight();
//            double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
//            String format = imageInfo.getFormat();
//            uploadPictureResult.setUrl(COS_HOST + "/" + uploadPath);
//            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
//            uploadPictureResult.setPicSize(FileUtil.size(file));
//            uploadPictureResult.setPicWidth(width);
//            uploadPictureResult.setPicHeight(height);
//            uploadPictureResult.setPicScale(picScale);
//            uploadPictureResult.setPicFormat(format);
//
//            return uploadPictureResult;
//        } catch (Exception e) {
//            log.error("图片上传到对象存储失败", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        } finally {
//            this.deleteTempFile(file);
//        }
//    }
//
//
//
//    @Override
//    public void validPicture(MultipartFile multipartFile) {
//        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传的图片不能为空");
//        ThrowUtils.throwIf(multipartFile.getSize() > FILE_SIZE_UPLOAD_LIMIT, ErrorCode.PARAMS_ERROR, "上传图片不能超过2MB");
//        ThrowUtils.throwIf(!FILE_TYPE_UPLOAD_LIMIT.contains(FileUtil.getSuffix(multipartFile.getOriginalFilename())),
//                ErrorCode.PARAMS_ERROR, "格式错误");
//    }
//
//    @Override
//    public void validPicture(String fileUrl) {
//        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
//
//        try {
//            // 1. 验证 URL 格式
//            new URL(fileUrl); // 验证是否是合法的 URL
//        } catch (MalformedURLException e) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
//        }
//
//        // 2. 校验 URL 协议
//        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
//                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
//
//        // 3. 发送 HEAD 请求以验证文件是否存在
//        HttpResponse response = null;
//        try {
//            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
//            // 未正常返回，无需执行其他判断
//            if (response.getStatus() != HttpStatus.HTTP_OK) {
//                return;
//            }
//            // 4. 校验文件类型
//            String contentType = response.header("Content-Type");
//            if (StrUtil.isNotBlank(contentType)) {
//                // 允许的图片类型
//                ThrowUtils.throwIf(!FILE_TYPE_UPLOAD_LIMIT.contains(contentType.toLowerCase()),
//                        ErrorCode.PARAMS_ERROR, "文件类型错误");
//            }
//            // 5. 校验文件大小
//            String contentLengthStr = response.header("Content-Length");
//            if (StrUtil.isNotBlank(contentLengthStr)) {
//                try {
//                    long contentLength = Long.parseLong(contentLengthStr);
//                    ThrowUtils.throwIf(contentLength > FILE_SIZE_UPLOAD_LIMIT, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
//                } catch (NumberFormatException e) {
//                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
//                }
//            }
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//        }
//    }
//    @Override
//    public void deleteTempFile(File file) {
//        if (file == null){
//            return;
//        }
//        // 删除文件
//        boolean delete = file.delete();
//        if (!delete){
//            log.error("file delete fail, filePath = {}", file.getAbsolutePath());
//        }
//    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 联合分类表
        if (StrUtil.isNotBlank(pictureEditRequest.getCategory())){
            QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("categoryName", pictureEditRequest.getCategory());
            Long id1 = pictureCategoryService.getOne(queryWrapper).getId();
            if (ObjectUtil.isNotEmpty(id1)){
                picture.setCategoryId(id1);
            }else {
                picture.setCategoryId(1L);
            }
        }
        // 处理标签
        List<String> tagIds = pictureEditRequest.getTags();
        pictureTagRelationService.handelTags(tagIds, pictureEditRequest.getId(), request);
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        User loginUser = userService.getCurrentUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 管理员自动过审
        this.filterReviewParam(picture, loginUser);
        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }


//    @Override
//    public void checkPictureAuth(User loginUser, Picture picture) {
//        Long spaceId = picture.getSpaceId();
//        Long loginUserId = loginUser.getId();
//        if (spaceId == null) {
//            // 公共图库，仅本人或管理员可操作
//            if (!picture.getUserId().equals(loginUserId) && !userService.isAdmin(loginUser)) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
//        } else {
//            // 私有空间，仅空间管理员可操作
//            if (!picture.getUserId().equals(loginUserId)) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
//        }
//    }


    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
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
        // 2. 判断是新增还是修改
        Long pictureId = null;
        if (pictureUploadRequest != null){
            pictureId = pictureUploadRequest.getId();
        }

        if (pictureId != null){
            // 校验是否存在数据
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            if (!exists){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不存在");
            }

            Picture oldPicture = this.getById(pictureId);
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
//        UploadPictureResult uploadPictureResult = this.uploadPicture(multipartFile, uploadPathPrefix);
        // 根据 inputSource 的类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构建entity
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureUploadRequest, pictureId);
        // 管理员自动过审
        this.filterReviewParam(picture, loginUser);

        // 开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 插入数据
            boolean result = this.saveOrUpdate(picture);
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

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询请求不存在");
        }
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        // 分类筛选
        String categoryName = pictureQueryRequest.getCategory();
        QueryWrapper<PictureCategory> queryWrapper1 = new QueryWrapper<>();
        long categoryId = 0;
        if (StrUtil.isNotBlank(categoryName)){
            categoryId = -1;
            queryWrapper1.eq("id", categoryName);
            PictureCategory pictureCategory = pictureCategoryService.getOne(queryWrapper1);
            if (pictureCategory != null){
                categoryId = pictureCategory.getId();
            }
        }

        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortFiled = pictureQueryRequest.getSortFiled();
        String sortOrder = pictureQueryRequest.getSortOrder();

        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        // 搜索框通知支持                                                                                                                                  名称和介绍检索
        if (StringUtils.isNotEmpty(searchText)){
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq( categoryId > 0 || categoryId == -1, "categoryId", categoryId);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        boolean flag = false;
        // 拿到所有标签id； 输入了标签就说明需要将标签作为查询条件；
        QueryWrapper<PictureTags> queryWrapper2 = new QueryWrapper<>();
        if (ObjUtil.isNotEmpty(tags)) {
            queryWrapper2.and(wrapper -> {
                for (String tagName : tags) {
                    wrapper.or().eq("tagName", tagName);
                }
            });
            flag = true;
        }

        List<Long> collect = pictureTagsService.list(queryWrapper2).stream().
                map(PictureTags::getId).collect(Collectors.toList());

        // 如果修改了flag状态(说明选中了标签)，才执行这些逻辑
        if (flag){
            // 查询所有需要查出来的记录；有标签id，去图片关联表查询
            boolean flag1 = false;
            QueryWrapper<PictureTagRelation> queryWrapper3 = new QueryWrapper<>();
            if (CollUtil.isNotEmpty(collect)) {
                queryWrapper3.and(wrapper -> {
                    for (long col : collect) {
                        wrapper.or().eq("tagId",col);
                    }
                });
                flag1 = true;
            }else {
                // 有标签 但图片标签表没有标签id,表示需要直接返回空；直接将-1作为查询条件；
                queryWrapper.eq("tagId",-1);
            }
            // 输入了标签，且这个标签在图片标签管理表中存在
            if (flag1){
                // 这里查询出来的就是需要展示的数据
                List<PictureTagRelation> list = pictureTagRelationService.list(queryWrapper3);
                // 有标签有标签id 查询; 没查到表示需要返回空，以-1作为查询条件；否则作为查询条件
                if (ObjUtil.isEmpty(list)){
                    // 有标签 但图片标签表没有标签id,表示需要直接返回空；直接将-1作为查询条件；
                    queryWrapper.eq("id",-1);
                }
                // 表示输入了标签，这些标签都有标签ID，需要将这些标签作为查询条件
                List<Long> col1 = list.stream().map(PictureTagRelation::getPictureId).collect(Collectors.toList());
                if (ObjUtil.isNotEmpty(col1)) {
                    queryWrapper.in("id", col1); // 添加多个 pictureId 条件
                }
            }
        }

        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);


        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortFiled), sortOrder.equals("ascend"), sortFiled);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVo(Picture picture) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserInfoVO userVO = userService.getUserVo(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPagePictureVo(Page<Picture> picturePage) {
        // 1. 参数校验
        // 图片集合
        List<Picture> pictureList = picturePage.getRecords();
        // 分页对象
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (pictureList == null){
            return pictureVOPage;
        }
        // 2. 集合转vo集合
        List<PictureVO> pictureVOList  = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //3. 给分页对象设置值
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVo(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 已是该状态
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        // 更新审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }


    @Override
    public void filterReviewParam(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)){
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    private static Picture getPicture(User loginUser, UploadPictureResult uploadPictureResult, PictureUploadRequest pictureUploadRequest,Long pictureId) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        if (StrUtil.isNotEmpty(pictureUploadRequest.getPicName())){
            picture.setName(pictureUploadRequest.getPicName());
        }else {
            picture.setName(uploadPictureResult.getPicName());
        }
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setSpaceId(pictureUploadRequest.getSpaceId()); // 指定空间 id

        picture.setPicColor(ColorTransformUtils.getStandardColor(uploadPictureResult.getPicColor()));

        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        return picture;
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限(已经改为使用注解鉴权)
        // checkPictureAuth(loginUser, oldPicture);
        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 私有空间，更新空间的使用额度，释放额度
            if (ObjectUtil.isNotEmpty(oldPicture.getSpaceId())){
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            // 删除图片标签表的记录
            QueryWrapper<PictureTagRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pictureId",pictureId);
            boolean remove = pictureTagRelationService.remove(queryWrapper);
            ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR);
            return true;
        });
    }


    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        // 3. 查询该空间下所有图片（必须有主色调）
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        // 如果没有图片，直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }
        // 将目标颜色转为 Color 对象
        Color targetColor = Color.decode(picColor);
        // 4. 计算相似度并排序
        List<Picture> sortedPictures = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    // 提取图片主色调
                    String hexColor = picture.getPicColor();
                    // 没有主色调的图片放到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 越大越相似
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                // 取前 12 个
                .limit(12)
                .collect(Collectors.toList());

        // 转换为 PictureVO
        return sortedPictures.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        // 拿到参数
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        // 3. 查询指定图片，仅选择需要的字段
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (pictureList.isEmpty()) {
            return;
        }
        // 4. 更新分类和标签
        // 批量替换分类ID
        QueryWrapper<PictureCategory> queryCategory = new QueryWrapper<>();
        queryCategory.eq("categoryName", category);
        Long categoryId = pictureCategoryService.getOne(queryCategory).getId();
        pictureList.forEach(picture -> {
                    if (ObjUtil.isNotEmpty(categoryId)) {
                        picture.setCategoryId(categoryId);
                    }
                });
        // 批量修改 图片标签表里的指定pictureList的 标签id
        List<String> tagTableList = pictureTagsService.list().stream().map(PictureTags::getTagName).
                collect(Collectors.toList());
        List<Long> tagIds = new ArrayList<>();
        for (String tagName: tags){
            // 拿到标签id
            if (tagTableList.contains(tagName)){
                QueryWrapper<PictureTags> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("tagName", tagName);
                tagIds.add(pictureTagsService.getOne(queryWrapper).getId());
            }
            // 将需要新增的标签添加到标签表，然后在获取他的id
            else {
                PictureTags pictureTags = new PictureTags();
                pictureTags.setTagName(tagName);
                pictureTagsService.save(pictureTags);
                tagIds.add(pictureTags.getId());
            }
        }
        // 清空之前的所有标签
        QueryWrapper<PictureTagRelation> queryWrapper = new QueryWrapper<>();
        List<Long> collect = pictureList.stream().map(Picture::getId).collect(Collectors.toList());
        queryWrapper.in("pictureId", collect);
        pictureTagRelationService.remove(queryWrapper);

        // 添加标签
        PictureTagRelation pictureTagRelation = new PictureTagRelation();
        pictureList.forEach(picture -> {
            tagIds.forEach(tagId -> {
                pictureTagRelation.setPictureId(picture.getId());
                pictureTagRelation.setTagId(tagId);
                pictureTagRelationService.save(pictureTagRelation);
            });
        });

        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);


        // 5. 批量更新
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * nameRule 格式：图片{序号}
     *
     * 用户批量编辑图片名称···································
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
        // 创建扩图任务
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        createOutPaintingTaskRequest.setInput(input);
        createOutPaintingTaskRequest.setParameters(createPictureOutPaintingTaskRequest.getParameters());
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(createOutPaintingTaskRequest);
    }


}




