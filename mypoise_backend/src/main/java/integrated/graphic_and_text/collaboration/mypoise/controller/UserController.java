package integrated.graphic_and_text.collaboration.mypoise.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserAddRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserLoginRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserRegisterRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.UserRoleEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static integrated.graphic_and_text.collaboration.mypoise.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant.EMAIL_PATTERN;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录
     * @param userLoginRequest 登录请求
     * @param httpServletRequest
     * @return 用户信息脱敏
     */
    @PostMapping("/login")
    public BaseResponse<UserInfoVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest){
        // 参数校验
        if (ObjectUtil.isEmpty(userLoginRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 用户登录
        UserInfoVO userInfoVO = userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword(), httpServletRequest);
        return ResultUtils.success(userInfoVO);
    }

    /**
     * 发送验证码
     * @param email
     * @return
     */
    @GetMapping("/captcha")
    public BaseResponse<Boolean> getCaptcha(String email){
        // 校验
        if (StringUtils.isEmpty(email)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱为空");
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        // 生成验证码
        String captcha = RandomUtil.randomNumbers(6);
        // 发送验证码
        try {
            userService.sendEmail(email, captcha);
            // 验证码存储redis，过期时间5分钟
            redisTemplate.opsForValue().set(CAPTCHA_CACHE_KEY + email,captcha,5, TimeUnit.MINUTES);
            return ResultUtils.success(true);
        } catch (MessagingException e) {
            log.error("【发送验证码失败】" + e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码获取失败");
        }

    }

    /**
     * 邮箱注册
     * @param request
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> doEmailRegister(@RequestBody UserRegisterRequest request){
        // 校验
        if (ObjectUtil.isEmpty(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 执行注册
        Long userId = userService.userRegister(request.getUserAccount(), request.getUserPassword(), request.getEmail(), request.getCaptcha());
        // 验证码删除
        redisTemplate.delete(CAPTCHA_CACHE_KEY + request.getEmail());
        return ResultUtils.success(userId);
    }

    /**
     * 查询当前登录用户
     * @param httpServletRequest 客户端对服务器的HTTP请求，包含请求方法、请求参数、请求头、cookie等
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserInfoVO> getLoginUser(HttpServletRequest httpServletRequest){
        User currentUser = userService.getCurrentUser(httpServletRequest);
        UserInfoVO loginUserVo = userService.getLoginUserVo(currentUser);
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 退出登录
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest httpServletRequest){
        Boolean b = userService.userLogout(httpServletRequest);
        return ResultUtils.success(b);
    }

    /**
     * 管理员 创建用户
     * @param userAddRequest
     * @return
     */
    @RequestMapping("/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> createUser(@RequestBody UserAddRequest userAddRequest){
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userAddRequest), ErrorCode.PARAMS_ERROR);
        // 创建用户记录，初始化数据
        User user = new User();
        BeanUtils.copyProperties(user, userAddRequest);
        // 设置默认密码
        String encryptPassword = userService.getEncryptPassword("12345678");
        user.setUserPassword(encryptPassword);
        // 添加记录
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员根据id获取用户
     * @param id
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/get/by/id")
    public BaseResponse<User> getUserById(long id){
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(user),ErrorCode.SYSTEM_ERROR, "未查询到指定用户");
        return ResultUtils.success(user);
    }

    /**
     * 查询用户脱敏信息
     * @param id
     * @return
     */
    @PostMapping("/get/vo/by/id")
    public BaseResponse<UserInfoVO> getUserVoById(long id){
        BaseResponse<User> user = getUserById(id);
        User getUser = user.getData();
        return ResultUtils.success(userService.getUserVo(getUser));
    }

    /**
     * 管理员删除用户
     * @param id
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(long id){
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        boolean b = userService.removeById(id);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员获取用户列表
     * @param userQueryRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/get/list/user/vo")
    public BaseResponse<Page<UserInfoVO>> getListUserVoByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userQueryRequest), ErrorCode.PARAMS_ERROR);
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        QueryWrapper<User> userQueryWrapper = userService.getUserQueryWrapper(userQueryRequest);
        // 分页, 参数1分页对象，参数2查询语句
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userQueryWrapper);
        /// 分页数据添加到 脱敏集合中
        List<UserInfoVO> listUserVo = userService.getListUserVo(userPage.getRecords());
        // 创建分页脱敏对象
        Page<UserInfoVO> userInfoVOPage = new Page<>(current, pageSize, userPage.getTotal());
        // 脱敏集合 添加到 分页脱敏集合中
        userInfoVOPage.setRecords(listUserVo);
        return ResultUtils.success(userInfoVOPage);
    }
}


















