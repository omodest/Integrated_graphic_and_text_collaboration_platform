package integrated.graphic_and_text.collaboration.mypoise.services;

import integrated.graphic_and_text.collaboration.mypoise.entity.dto.wx.WxUserInfo;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.UserWechat;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.factory.annotation.Value;

/**
* @author poise
* @description 针对表【user_wechat】的数据库操作Service
* @createDate 2025-01-10 11:26:41
*/
public interface UserWechatService extends IService<UserWechat> {


//    /**
//     * 微信绑定
//     * @param userId
//     * @param openid
//     * @param unionid
//     */
//    void bindWeChat(Long userId, String openid, String unionid);
//
//    /**
//     * 小程序登录
//     * @param openid
//     * @param wxUserInfo
//     * @return
//     */
////    User createOrUpdateUser(String openid, WxUserInfo wxUserInfo);
//
//    String getAuthorizationUrl();
//
//    WxUserInfo getWxUserInfo(String code) throws Exception;
}
