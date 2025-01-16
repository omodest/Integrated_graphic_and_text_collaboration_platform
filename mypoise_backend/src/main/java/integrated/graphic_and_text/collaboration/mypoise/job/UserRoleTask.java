package integrated.graphic_and_text.collaboration.mypoise.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import integrated.graphic_and_text.collaboration.mypoise.utils.RedissonLockUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 定时任务- 实时更新用户权限
 */
@Component
public class UserRoleTask {

    @Resource
    private RedissonLockUtils redissonLockUtils;

    @Resource
    private UserService userService;

    /**
     * 用户的优惠卷过期，定时任务，将user_coupon中的状态修改为 2(已过期)
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void couponConfirm() {
        // 使用 Redisson 分布式锁，确保该操作不会被多个实例同时执行
        redissonLockUtils.redissonDistributedLocks("userRoleConfirm", () -> {
            // 1. 获取所有权限为VIP用户信息
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userRole", UserConstant.VIP_ROLE);
            List<User> userList = userService.list(queryWrapper);
            // 2. 校验权限是否过期
            LocalDateTime currentDate = LocalDateTime.now();
            for (User user : userList) {
                // 转换 Vip 过期时间为 LocalDateTime 进行比较
                LocalDateTime vipExpire = LocalDateTime.ofInstant(user.getVip_expire().toInstant(), ZoneId.systemDefault());
                if (vipExpire.isBefore(currentDate)) {
                    user.setUserRole(UserConstant.USER_ROLE);
                    userService.updateById(user);
                }
            }
        });
    }
}
