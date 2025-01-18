package integrated.graphic_and_text.collaboration.mypoise.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.DeleteRequest;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureEditRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture.PictureUpdateRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureCategory;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.PictureVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureCategoryService;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/picture/category")
@Slf4j
public class PictureCategoryController {

    @Resource
    private PictureCategoryService pictureCategoryService;

    @Resource
    private PictureService pictureService;

    /**
     * 创建分类
     * @param categoryName 标签名
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addCategory(String categoryName){
        ThrowUtils.throwIf(StrUtil.isEmpty(categoryName), ErrorCode.PARAMS_ERROR, "分类为空");
        PictureCategory pictureCategory = new PictureCategory();
        pictureCategory.setCategoryName(categoryName);
        boolean save = pictureCategoryService.save(pictureCategory);
        ThrowUtils.throwIf(!save, ErrorCode.PARAMS_ERROR, "系统错误，可能是分类重复导致的。");
        return ResultUtils.success(true);
    }

    /**
     * 删除分类
     * @param categoryId 删除标识
     * @return
     */
    @PostMapping("/del")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delCategory(long categoryId){
        // 1. 参数校验
        ThrowUtils.throwIf(categoryId <= 1, ErrorCode.PARAMS_ERROR);
        // 2. 将当前分类的图片，都改为默认分类
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("categoryId",categoryId);
        List<Picture> pictureList = pictureService.list(queryWrapper);
        List<Object> collect = pictureList.stream().map(picture -> {
            picture.setCategoryId(1L);
            return null;
        }).collect(Collectors.toList());
        pictureService.updateBatchById(pictureList);
        return ResultUtils.success(pictureCategoryService.removeById(categoryId));
    }

    /**
     * 编辑
     * @return
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editCategory(long categoryId, String categoryName){
        ThrowUtils.throwIf(StrUtil.isEmpty(categoryName), ErrorCode.PARAMS_ERROR, "分类为空");
        PictureCategory pictureCategory = pictureCategoryService.getById(categoryId);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureCategory), ErrorCode.PARAMS_ERROR, "数据不存在");
        pictureCategory.setCategoryName(categoryName);
        return ResultUtils.success(pictureCategoryService.updateById(pictureCategory));
    }

    /**
     * 查询所有分类(按照使用频率排序返回)
     * @return
     */
    @PostMapping("/get")
    public BaseResponse<List<PictureCategory>> queryCategory(){
        QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("applyTotal");
        // 查询热门分类
        List<PictureCategory> pictureCategoryList = pictureCategoryService.list(queryWrapper);
        return ResultUtils.success(pictureCategoryList);
    }

}
