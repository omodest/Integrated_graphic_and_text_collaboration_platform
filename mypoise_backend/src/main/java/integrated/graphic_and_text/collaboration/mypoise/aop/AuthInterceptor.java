package integrated.graphic_and_text.collaboration.mypoise.aop;

import integrated.graphic_and_text.collaboration.mypoise.annotation.AuthCheck;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.UserRoleEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 权限校验-切面
     * @param proceedingJoinPoint 切入点
     * @param authCheck 需要的权限
     * @return
     */
    @Around("@annotation(authCheck)") // 环绕切面，在该切入点的周围生效
    public Object doInterceptor(ProceedingJoinPoint proceedingJoinPoint, AuthCheck authCheck) throws Throwable {
        // 1. 拿到 httpServletRequest，目标是拿到用户信息
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 2. 拿到当前需要的权限
        String needRole = authCheck.mustRole();
        UserRoleEnum enumNeedRole = UserRoleEnum.getEnumByValue(needRole);
        // 3. 拿到用户的权限
        User user = userService.getCurrentUser(request);
        String userRole = user.getUserRole();
        UserRoleEnum enumUserRole = UserRoleEnum.getEnumByValue(userRole);
        // 4. 进行判断

        // 不需要权限，执行放行
        if (enumNeedRole == null){
            return proceedingJoinPoint.proceed();
        }
        // 需要权限,但没有；如果需要管理员权限，但不是管理员抛异常
        if (enumUserRole == null || (UserRoleEnum.ADMIN.equals(enumNeedRole) && !UserRoleEnum.ADMIN.equals(enumUserRole))){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //
        if (UserRoleEnum.VIP.equals(enumNeedRole) || UserRoleEnum.FVIP.equals(enumNeedRole)){
            if (UserRoleEnum.VIP.equals(enumUserRole) || UserRoleEnum.FVIP.equals(enumUserRole)){
                return proceedingJoinPoint.proceed();
            }
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return proceedingJoinPoint.proceed();
    }

}

















