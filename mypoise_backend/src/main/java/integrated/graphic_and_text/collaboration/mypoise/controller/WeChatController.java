package integrated.graphic_and_text.collaboration.mypoise.controller;

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
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.*;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.wx.WxUserInfo;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserWechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}


















