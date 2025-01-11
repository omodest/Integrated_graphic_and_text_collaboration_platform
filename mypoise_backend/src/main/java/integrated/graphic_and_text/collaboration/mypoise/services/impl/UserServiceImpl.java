package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import integrated.graphic_and_text.collaboration.mypoise.config.EmailConfig;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.mapper.UserMapper;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import integrated.graphic_and_text.collaboration.mypoise.utils.EmailUtils;
import integrated.graphic_and_text.collaboration.mypoise.utils.RedissonLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static integrated.graphic_and_text.collaboration.mypoise.constant.EmailConstant.*;
import static integrated.graphic_and_text.collaboration.mypoise.constant.LockConstant.EMAIL_REGISTER;
import static integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant.EMAIL_PATTERN;
import static integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author poise
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-01-10 11:26:40
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    /**
     * 引入发送验证码的依赖
     */
    @Resource
    private JavaMailSender mailSender;

    /**
     * 邮箱配置
     */
    @Resource
    private EmailConfig emailConfig;

    /**
     * redis模板
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分布式锁
     */
    @Resource
    private RedissonLockUtils redissonLockUtils;

    @Override
    public UserInfoVO userLogin(String userAccount, String password, HttpServletRequest httpServletRequest) {
        // 1. 验证用户信息
        if (StrUtil.hasBlank(userAccount, password) || userAccount.length() < 4 || password.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号密码输入错误");
        }
        // 2. 密码加密（对用户输入的密码进行加密）
        String encryptPassword = getEncryptPassword(password);
        // 3. 校验是否能登录
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 3. 登录
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，或者请求参数错误");
        }
        // 4. 存session
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5. 返回给前端的用户信息脱敏
        return getLoginUserVo(user);
    }

    @Override
    public Long userRegister(String userAccount, String password,String email, String captcha) {
        // 1. 参数校验
        if (StrUtil.hasBlank(userAccount, email, captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() > 40){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称过长");
        }
        if (password.length() > 40 || password.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度错误");
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        // 2. 验证码校验
        String cacheCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
        if (StringUtils.isEmpty(cacheCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码过期");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        // 3. 分布式锁进行数据添加
        // 创建锁名称（intern 确保字符串常量池中只有一个相同的字符串对象）
        String redissonLock = (EMAIL_REGISTER + userAccount).intern();

        // 分布式锁，参数1：分布式锁的名称；参数2: 需要加锁的代码块(理解为比如说某段代码需要用try catch抛出，或者事务....)
        return redissonLockUtils.redissonDistributedLocks(redissonLock, () -> {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", email);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName("AAA");
            String encryptPassword = getEncryptPassword(password);
            user.setUserPassword(encryptPassword);
            user.setEmail(email);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
            // 工具类中已经对锁进行乐释放--------
        }, "邮箱账号注册失败");
    }

    @Override
    public void sendEmail(String emailAccount, String captcha) throws MessagingException {
        // 1. 创建邮箱API
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // 2. 构建邮件内容
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        // 设置邮件主题
        mimeMessageHelper.setSubject(EMAIL_SUBJECT);
        // 使用工具类构建文件内容
        String emailContent = EmailUtils.buildEmailContent(EMAIL_HTML_CONTENT_PATH, captcha);
        // 接收者邮箱地址
        mimeMessageHelper.setText(emailContent, true);
        // 邮件的发件人地址
        mimeMessageHelper.setTo(emailAccount);
        mimeMessageHelper.setFrom(EMAIL_TITLE + '<' + emailConfig.getEmailFrom() + '>');
        // 3. 发送
        mailSender.send(mimeMessage);
    }

    @Override
    public String getEncryptPassword(String password) {
        /*
         * 盐值，混淆密码
         */
        final String SALT = "Cloud_POISE";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    @Override
    public UserInfoVO getLoginUserVo(User user) {
        if (user == null){
            return null;
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user,userInfoVO);
        return userInfoVO;
    }

    @Override
    public User getCurrentUser(HttpServletRequest httpServletRequest) {
        // 尝试从 session中获取当前登录用户
        User user = (User)httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getId() <= 0){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 查数据库；防止因中途用户销号，但是session缓存中数据依然存在的问题
        Long userId = user.getId();
        User getUserById = this.getById(userId);
        if (getUserById == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return getUserById;
    }

    @Override
    public Boolean userLogout(HttpServletRequest httpServletRequest) {
        User user = (User)httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserInfoVO getUserVo(User user) {
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    public List<UserInfoVO> getListUserVo(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userList.stream().map(this::getUserVo).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getUserQueryWrapper(UserQueryRequest userQueryRequest) {
        // 参数校验
        if (ObjectUtil.isEmpty(userQueryRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询请求对象为空");
        }
        // 获取请求参数
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortFiled = userQueryRequest.getSortFiled();
        String sortOrder = userQueryRequest.getSortOrder();
        // 拼接queryWrapper
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(id),"id", id);
        queryWrapper.like(StrUtil.isNotEmpty(userName),"userName", userName);
        queryWrapper.like(StrUtil.isNotEmpty(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotEmpty(userProfile), "userProfile", userProfile);
        queryWrapper.eq(StrUtil.isNotEmpty(userRole), "userRole", userRole);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortFiled),sortOrder.equals("ascend"), sortOrder);

        return queryWrapper;
    }
}




