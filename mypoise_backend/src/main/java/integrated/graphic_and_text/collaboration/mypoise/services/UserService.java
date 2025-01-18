package integrated.graphic_and_text.collaboration.mypoise.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserUpdateRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author poise
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-01-10 11:26:41
*/
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param userAccount 账号
     * @param password 密码
     * @return 脱敏信息
     */
    UserInfoVO userLogin(String userAccount, String password, HttpServletRequest httpServletRequest);

//    /**
//     * 微信扫码登录
//     * @param userAccount
//     * @param password
//     * @return
//     */
//    UserInfoVO userLogin();


    /**
     * 邮箱登录验证
     * @return
     */
    User userEmailLogin(String email, String captcha, HttpServletRequest httpServletRequest);


    /**
     * 邮箱注册
     * @param userAccount 用户账号
     * @param email 邮箱
     * @param captcha 用户输入的验证码
     * @return
     */
    Long userRegister(String userAccount, String password, String email, String captcha);

    /**
     * 邮箱修改密码
     * @param email
     * @param captcha
     * @return
     */
    boolean userEmailEditPwd(String email, String captcha,String userPassword,HttpServletRequest httpServletRequest);

    /**
     * 邮箱绑定
     * @param email
     * @param captcha
     * @param httpServletRequest
     * @return
     */
    boolean userBind(String email, String captcha, HttpServletRequest httpServletRequest);

    /**
     * 发送验证码
     * @param emailAccount 接收邮箱
     * @param captcha 验证码
     */
    void sendEmail(String emailAccount, String captcha) throws MessagingException;

//    /**
//     * 微信扫码注册
//     * @return
//     */
//    Long userRegister();


    /**
     * 密码加密
     * @param password 密码
     * @return 加密后密码
     */
    String getEncryptPassword(String password);

    /**
     * 用户信息脱敏
     * @param user 用户信息
     * @return 用户脱敏信息
     */
    UserInfoVO getLoginUserVo(User user);

    /**
     * 获取当前登录用户信息
     * @param httpServletRequest
     * @return
     */
    User getCurrentUser(HttpServletRequest httpServletRequest);

    /**
     * 用户登录态 注销
     * @param httpServletRequest
     * @return
     */
    Boolean userLogout(HttpServletRequest httpServletRequest);

    /**
     * 获取单个用户的脱敏信息
     * @param user
     * @return
     */
    UserInfoVO getUserVo(User user);

    /**
     * 获取脱敏用户信息列表
     * @param userList
     * @return
     */
    List<UserInfoVO> getListUserVo(List<User> userList);

    /**
     * 拼接对用户查询
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getUserQueryWrapper(UserQueryRequest userQueryRequest);



    /**
     * 简单用户签到
     * @return
     */
    boolean doCurrentDaySign(HttpServletRequest httpServletRequest);

    /**
     * 获取到当天 连续签到的天数
     * @param httpServletRequest
     * @return
     */
    Integer getConstantSignDay(HttpServletRequest httpServletRequest);

    /**
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

}
