package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.UserWechat;
import integrated.graphic_and_text.collaboration.mypoise.mapper.UserWechatMapper;
import integrated.graphic_and_text.collaboration.mypoise.services.UserWechatService;
import org.springframework.stereotype.Service;


/**
* @author poise
* @description 针对表【user_wechat】的数据库操作Service实现
* @createDate 2025-01-10 11:26:41
*/
@Service
public class UserWechatServiceImpl extends ServiceImpl<UserWechatMapper, UserWechat>
    implements UserWechatService {

//    @Resource
//    private WxConfig wxConfig;
//
//    @Resource
//    private UserService userService;
//
//    @Override
//    public void bindWeChat(Long userId, String openid, String unionid) {
//        // 1. 判断微信是否已经绑定
//        QueryWrapper<UserWechat> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("open_id", openid);
//        UserWechat findOpenid = this.getOne(queryWrapper);
//        if (ObjectUtil.isEmpty(findOpenid)) {
//            throw new RuntimeException("微信账号已被绑定");
//        }
//        // 2. 绑定赋值
//        UserWechat userWechat = new UserWechat();
//        userWechat.setOpenid(openid);
//        userWechat.setUnionid(unionid);
//        this.save(userWechat);
//        // 3. 联表赋值
//        User user = userService.getById(userId);
//        user.setWechat_id(userWechat.getId());
//    }
//
////    @Override
////    public User createOrUpdateUser(String openid, WxUserInfo wxUserInfo) {
////        User user = .findByOpenid(openid);
////        if (user == null) {
////            user = new User();
////            user.setOpenid(openid);
////        }
////        // 更新用户信息
////        user.setNickname(wxUserInfo.getNickName());
////        user.setAvatar(wxUserInfo.getAvatarUrl());
////        user.setGender(wxUserInfo.getGender());
////        return userRepository.save(user);
////    }
//
//
//
//    // 生成微信授权URL
//    @Override
//    public String getAuthorizationUrl() {
//        return String.format(
//                "https://open.weixin.qq.com/connect/qrconnect?" +
//                        "appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=test123#wechat_redirect",
//                wxConfig.getAppid(), URLEncoder.encode(wxConfig.getRedirectUri()));
//    }
//
//    // 获取微信用户信息
//    @Override
//    public WxUserInfo getWxUserInfo(String code) throws Exception {
//        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
//                "appid=" + wxConfig.getAppid() +
//                "&secret=" + wxConfig.getSecret() +
//                "&code=" + code +
//                "&grant_type=authorization_code";
//
//        // 获取access_token
//        String response = HttpClientUtils.get(url);
//        JSONObject tokenObj = JSON.parseObject(response);
//
//        // 获取用户信息
//        String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?" +
//                "access_token=" + tokenObj.getString("access_token") +
//                "&openid=" + tokenObj.getString("openid");
//
//        String userInfoResponse = HttpClientUtils.get(userInfoUrl);
//        return JSON.parseObject(userInfoResponse, WxUserInfo.class);
//    }
}




