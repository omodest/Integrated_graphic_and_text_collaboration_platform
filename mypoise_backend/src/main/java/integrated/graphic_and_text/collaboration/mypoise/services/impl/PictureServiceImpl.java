package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureUploadRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.file.UploadPictureResult;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.manage.CosManager;
import integrated.graphic_and_text.collaboration.mypoise.mapper.PictureMapper;
import integrated.graphic_and_text.collaboration.mypoise.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static integrated.graphic_and_text.collaboration.mypoise.constant.FileConstant.*;

/**
* @author poise
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-01-17 16:25:07
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    /**
     * COS 文件 操作
     */
    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;

    @Resource
    private PictureCategoryService pictureCategoryService;

    @Resource
    private PictureTagsService pictureTagsService;

    @Resource
    private PictureTagRelationService pictureTagRelationService;
    @Override
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 1. 图片校验
        validPicture(multipartFile);
        // 2. 拼接存储路径
        String uuid = RandomUtil.randomNumbers(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadPath = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        // 3. 上传图片
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(tempFile);
            // 上传图片（数据万象）
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, tempFile);
            // 拿到数据万向分析出来的图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult()
                    .getOriginalInfo()
                    .getImageInfo();

            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            Integer width = imageInfo.getWidth();
            Integer height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
            String format = imageInfo.getFormat();
            uploadPictureResult.setUrl(COS_HOST + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(format);

            return uploadPictureResult;
        } catch (IOException e) {
            log.error("数据上传到对象存储失败。(数据万象)");
            // 处理文件上传异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        }finally {
            // 删除临时文件
            deleteTempFile(tempFile);
        }
    }

    @Override
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传的图片不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > FILE_SIZE_UPLOAD_LIMIT, ErrorCode.PARAMS_ERROR, "上传图片不能超过2MB");
        ThrowUtils.throwIf(!FILE_TYPE_UPLOAD_LIMIT.contains(FileUtil.getSuffix(multipartFile.getOriginalFilename())),
                ErrorCode.PARAMS_ERROR, "格式错误");
    }

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
    public void deleteTempFile(File file) {
        if (file == null){
            return;
        }
        // 删除文件
        boolean delete = file.delete();
        if (!delete){
            log.error("file delete fail, filePath = {}", file.getAbsolutePath());
        }
    }

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未找到用户信息");
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
        }
        // 3. 增加数据库记录
        // 按照用户id划分目录
        String uploadPathPrefix  = String.format("public/%s", loginUser.getId());

        // 调用 数据万象的图片上传
        UploadPictureResult uploadPictureResult = this.uploadPicture(multipartFile, uploadPathPrefix);
        // 构建entity
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureId);

        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
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

        String categoryName = pictureQueryRequest.getCategoryName();
        QueryWrapper<PictureCategory> queryWrapper1 = new QueryWrapper<>();
        long categoryId = 0;
        if (StrUtil.isNotBlank(categoryName)){
            categoryId = -1;
            queryWrapper1.eq("categoryName", categoryName);
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
        // 搜索框通知支持                                                                                                                                  名称和介绍检索
        if (StringUtils.isNotEmpty(searchText)){
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }
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
        if (ObjUtil.isNotEmpty(tags)){
            for (String tagName: tags){
                flag = true;
                queryWrapper2.eq("tagName", tagName);
            }
        }
        List<Long> collect = pictureTagsService.list(queryWrapper2).stream().
                map(PictureTags::getId).collect(Collectors.toList());

        // 如果修改了flag状态，才执行这些逻辑
        if (flag){
            // 查询所有需要查出来的记录；有标签id，去图片关联表查询
            boolean flag1 = false;
            QueryWrapper<PictureTagRelation> queryWrapper3 = new QueryWrapper<>();
            if (CollUtil.isNotEmpty(collect)) {
                for (long col : collect) {
                    flag1 = true;
                    queryWrapper3.eq("tagId",col);
                }
            }else {
                // 有标签 但图片标签表没有标签id,表示需要直接返回空；直接将-1作为查询条件；
                queryWrapper.eq("id",-1);
            }
            // 输入了标签，且这个标签在图片标签管理表中存在
            if (flag1){
                List<PictureTagRelation> list = pictureTagRelationService.list(queryWrapper3);
                // 有标签有标签id 查询; 没查到表示需要返回空，以-1作为查询条件；否则作为查询条件
                if (ObjUtil.isEmpty(list)){
                    // 有标签 但图片标签表没有标签id,表示需要直接返回空；直接将-1作为查询条件；
                    queryWrapper.eq("id",-1);
                }
                // 表示输入了标签，这些标签都有标签ID，需要将这些标签作为查询条件
                List<Long> col1 = list.stream().map(PictureTagRelation::getPictureId).collect(Collectors.toList());
                if (ObjUtil.isEmpty(col1)) {
                    queryWrapper3.in("pictureId", col1); // 添加多个 pictureId 条件
                }
            }
        }

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

    private static Picture getPicture(User loginUser, UploadPictureResult uploadPictureResult, Long pictureId) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        return picture;
    }
}




