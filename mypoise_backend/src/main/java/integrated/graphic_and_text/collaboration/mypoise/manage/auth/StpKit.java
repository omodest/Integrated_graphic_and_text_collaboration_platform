package integrated.graphic_and_text.collaboration.mypoise.manage.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 *
 * 该类的主要目的是集中管理项目中不同类型的账号体系（如默认账号体系和空间账号体系），
 * 并提供静态访问点，方便在项目中统一调用。
 */
@Component
public class StpKit {

    /**
     * 空间账号体系的类型标识符。
     * 该常量用于区分空间账号体系与其他账号体系（如默认账号体系）。
     */
    public static final String SPACE_TYPE = "space";


    /**
     * Space 会话对象，用于管理 Space 表所有账号的登录状态和权限认证。
     * 该对象是一个 StpLogic 实例，专门用于处理与空间账号相关的操作，
     * 例如登录、注销、权限校验等。
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);

    /**
     * 默认原生会话对象，项目中目前没使用到
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;


}