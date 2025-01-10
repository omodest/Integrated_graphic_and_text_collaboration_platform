package integrated.graphic_and_text.collaboration.mypoise.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserLoginRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserRegisterRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
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
}
