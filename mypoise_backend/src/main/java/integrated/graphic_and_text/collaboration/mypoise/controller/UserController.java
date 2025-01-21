package integrated.graphic_and_text.collaboration.mypoise.controller;
import java.util.Date;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
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
    private StringRedisTemplate stringRedisTemplate;

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
     * 邮箱登录
     * @return
     */
    @PostMapping("/login/email")
    public BaseResponse<UserInfoVO> userLoginByEmail(@RequestBody UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userEmailLoginRequest), ErrorCode.PARAMS_ERROR);
        // 验证邮箱、验证验证码
        User user = userService.userEmailLogin(userEmailLoginRequest.getEmail(), userEmailLoginRequest.getCaptcha(), httpServletRequest);
        return ResultUtils.success(userService.getUserVo(user));
    }

    /**
     * 邮箱修改密码
     * @param userEmailEditPwdRequest
     * @return
     */
    @PostMapping("/email/edit/password")
    public BaseResponse<Boolean> userEmailEditPassword(@RequestBody UserEmailEditPwdRequest userEmailEditPwdRequest, HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userEmailEditPwdRequest), ErrorCode.PARAMS_ERROR);
        String email = userEmailEditPwdRequest.getEmail();
        String captcha = userEmailEditPwdRequest.getCaptcha();
        String userPassword = userEmailEditPwdRequest.getUserPassword();
        boolean b = userService.userEmailEditPwd(email, captcha, userPassword,httpServletRequest);
        return ResultUtils.success(b);
    }

    /**
     * 邮箱绑定
     * @param userBindRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/email/bind")
    public BaseResponse<Boolean> userEmailBind(@RequestBody UserBindRequest userBindRequest, HttpServletRequest httpServletRequest){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userBindRequest), ErrorCode.PARAMS_ERROR);
        boolean b = userService.userBind(userBindRequest.getEmail(), userBindRequest.getCaptcha(), httpServletRequest);
        // 验证码删除
        stringRedisTemplate.delete(CAPTCHA_CACHE_KEY + userBindRequest.getEmail());
        return ResultUtils.success(b);
    }


    /**
     * 发送验证码
     * @param email
     * @param repeat 是否允许重复邮箱
     * @return
     */
    @GetMapping("/captcha")
    public BaseResponse<Boolean> getCaptcha(String email, boolean repeat){
        // 校验
        if (StringUtils.isEmpty(email)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱为空");
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }

        if (!repeat){
            // 一个邮箱只能注册一个账号
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("email", email);
            List<User> list = userService.list(wrapper);
            if (!list.isEmpty()){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱重复");
            }
        }


        // 生成验证码
        String captcha = RandomUtil.randomNumbers(6);
        // 发送验证码
        try {
            userService.sendEmail(email, captcha);
            // 验证码存储redis，过期时间5分钟
            stringRedisTemplate.opsForValue().set(CAPTCHA_CACHE_KEY + email,captcha,5, TimeUnit.MINUTES);
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误，邮箱注册账号失败");
        }
        // 执行注册
        Long userId = userService.userRegister(request.getUserAccount(), request.getUserPassword(), request.getEmail(), request.getCaptcha());
        // 验证码删除
        stringRedisTemplate.delete(CAPTCHA_CACHE_KEY + request.getEmail());
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
    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> createUser(@RequestBody UserAddRequest userAddRequest){
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userAddRequest), ErrorCode.PARAMS_ERROR);
        // 创建用户记录，初始化数据
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 设置默认密码
        String encryptPassword = userService.getEncryptPassword("12345678");
        user.setUserPassword(encryptPassword);
        user.setUserAvatar("https://img2.baidu.com/it/u=3037564649,1385361338&fm=253&fmt=auto&app=138&f=JPEG?w=296&h=360");
        // 添加记录
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员修改用户信息
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInfoVO> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userUpdateRequest), ErrorCode.PARAMS_ERROR);
        // 修改数据
        User currentUser = userService.getById(userUpdateRequest.getId());
        ThrowUtils.throwIf(currentUser == null, ErrorCode.PARAMS_ERROR);
        BeanUtils.copyProperties(userUpdateRequest, currentUser);
        // 修改数据库
        userService.updateById(currentUser);
        return ResultUtils.success(userService.getLoginUserVo(currentUser));
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

    @PostMapping("/edit/my/vo")
    @AuthCheck(mustRole = UserConstant.USER_ROLE)
    public BaseResponse<UserInfoVO> userEditMyVo(@RequestBody UserInfoVO userInfoVO, HttpServletRequest httpServletRequest){
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userInfoVO), ErrorCode.PARAMS_ERROR);
        // 身份校验
        Long currentUserId = userService.getCurrentUser(httpServletRequest).getId();
        ThrowUtils.throwIf(!Objects.equals(currentUserId, userInfoVO.getId()), ErrorCode.NO_AUTH_ERROR, "仅限本人可修改自己的信息");
        // 修改数据
        User user = new User();
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
        BeanUtils.copyProperties(userInfoVO, user);
        userService.updateById(user);
        // 编辑
        return ResultUtils.success(userService.getUserVo(user));
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

    /**
     * 签到操作
     * @param httpServletRequest 用来获取用户信息
     * @return 操作结果
     */
    @PostMapping("/doSign")
    public BaseResponse<Boolean> doSign(HttpServletRequest httpServletRequest){
        boolean done = userService.doCurrentDaySign(httpServletRequest);
        return ResultUtils.success(done);
    }

    /**
     * 统计签到次数
     * @param httpServletRequest 用来获取用户信息
     * @return 操作结果
     */
    @GetMapping("/get/totalSign")
    public BaseResponse<Integer> getSignNum(HttpServletRequest httpServletRequest){
        Integer constantSignDay = userService.getConstantSignDay(httpServletRequest);
        return ResultUtils.success(constantSignDay);
    }
}


















