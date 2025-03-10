package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import integrated.graphic_and_text.collaboration.mypoise.config.EmailConfig;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.user.UserUpdateRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.UserRoleEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.StpKit;
import integrated.graphic_and_text.collaboration.mypoise.mapper.UserMapper;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import integrated.graphic_and_text.collaboration.mypoise.utils.EmailUtils;
import integrated.graphic_and_text.collaboration.mypoise.utils.RedissonLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
    private StringRedisTemplate stringRedisTemplate;

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
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 3. 登录
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，或者请求参数错误");
        }
        // 4. 存session
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        // 5. 返回给前端的用户信息脱敏
        return getLoginUserVo(user);
    }

    @Override
    public User userEmailLogin(String email, String captcha, HttpServletRequest httpServletRequest) {
        // 1. 参数校验
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 验证码校验
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
        if (StringUtils.isEmpty(cacheCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码过期!!!");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        // 3. 根据email找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = this.getOne(queryWrapper);
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return user;
    }

    @Override
    public boolean userEmailEditPwd(String email, String captcha, String userPassword, HttpServletRequest httpServletRequest) {
        User user = this.userEmailLogin(email, captcha,  httpServletRequest);
        String encryptPassword = this.getEncryptPassword(userPassword);
        user.setUserPassword(encryptPassword);
        return this.updateById(user);
    }

    @Override
    public boolean userBind(String email, String captcha, HttpServletRequest httpServletRequest) {
        // 拿到用户
        User currentUser = this.getCurrentUser(httpServletRequest);
        // 发送验证码、验证验证码
        // 1. 参数校验
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 验证码校验
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
        if (StringUtils.isEmpty(cacheCaptcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码过期!!!");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        // 添加记录
        currentUser.setEmail(email);
        return this.updateById(currentUser);
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
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + email);
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
            // 工具类中已经对锁进行释放--------
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
    public User getCurrentUser(@NotNull HttpServletRequest httpServletRequest) {
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


    @Override
    public boolean doCurrentDaySign(HttpServletRequest httpServletRequest) {
        // 1. 验证当前用户
        User loginUser = this.getCurrentUser(httpServletRequest);
        Long loginUserId = loginUser.getId();
        ThrowUtils.throwIf(loginUserId <= 0,ErrorCode.PARAMS_ERROR,"登录状态异常");
        // 2. 获取当天日期 年+月 的格式
        LocalDateTime now = LocalDateTime.now();
        String nowFormat = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 3. 使用用户唯一标识 + 当前日期，作为存redis 时的唯一标识
        String key = "sign-" + loginUserId + "-" + nowFormat;
        // 4. 获取今天是本月第几天，好用来实现签到功能
        int dayOfMonth = now.getDayOfMonth();
        // 5. 写redis操作
        Boolean result = stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        ThrowUtils.throwIf(Boolean.TRUE.equals(result),ErrorCode.OPERATION_ERROR,"今日已签到");
        // 6. 签到的奖励可以写到这里, 目前系统设计的是连续签到七天 获得1天会员;连续签到30天送 5天会员
        // 获取当前日期和时间
        Date currentDate = new Date();
        Integer constantSignDay = this.getConstantSignDay(httpServletRequest);
        // 使用 Calendar 增加时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        if (constantSignDay != 0 && constantSignDay % 7 == 0){
            calendar.add(Calendar.DAY_OF_MONTH, 1); // 增加 5 天
            loginUser.setVip_expire(calendar.getTime());
        }
        if (constantSignDay != 0 && constantSignDay % 30 == 0){
            calendar.add(Calendar.DAY_OF_MONTH, 5); // 增加 5 天
            loginUser.setVip_expire(calendar.getTime());
        }
        loginUser.setUserRole(loginUser.getUserRole());
        loginUser.setEditTime(currentDate);
        this.updateById(loginUser);
        return true;
    }

    //  这里可以添加连续多少多少天签到，获得奖励
    @Override
    public Integer getConstantSignDay(HttpServletRequest httpServletRequest) {
        // 1. 验证当前用户
        User loginUser = this.getCurrentUser(httpServletRequest);
        Long loginUserId = loginUser.getId();
        ThrowUtils.throwIf(loginUserId <= 0,ErrorCode.PARAMS_ERROR,"登录状态异常");
        // 2. 获取当天日期 年+月 的格式
        LocalDateTime now = LocalDateTime.now();
        String nowFormat = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 3. 使用用户唯一标识 + 当前日期，作为存redis 时的唯一标识
        String key = "sign-" + loginUserId + "-" + nowFormat;
        // 4. 获取今天是本月第几天，好用来实现查询
        int dayOfMonth = now.getDayOfMonth();

        // 5.获取本月截止今天为止的所有的签到记录，返回的是一个十进制的数字 BITFIELD sign:5:202403 GET U14 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()){
            return 0;
        }
        Long num = result.get(0);
        if (num == 0){
            return 0;
        }
        // 6. 循环遍历
        int count = 0;
        // 签到天数统计
        // 任何数a 与 1 做与运输，结果都是a，所以这里用来判断当天是否签到
        while ((num & 1) != 0) {
            count++;
            // 右移，表示查找前上一天
            num >>>= 1;
        }
        return count;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}




