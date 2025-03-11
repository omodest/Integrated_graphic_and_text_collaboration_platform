package integrated.graphic_and_text.collaboration.mypoise.manage.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.SpaceRoleEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.SpaceTypeEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Space;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.SpaceUser;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.model.SpaceUserAuthConfig;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.model.SpaceUserPermissionConstant;
import integrated.graphic_and_text.collaboration.mypoise.manage.auth.model.SpaceUserRole;
import integrated.graphic_and_text.collaboration.mypoise.services.SpaceUserService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空间成员权限管理
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 封装了角色列表 和 权限列表的实体类
     */
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    /**
     * 静态内部类加载配置文件，获取数据，保证角色权限能在初始化时加载
     */
    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     *
     * @param spaceUserRole
     * @return
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }

        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles()
                .stream()
                .filter(r -> r.getKey().equals(spaceUserRole))
                .findFirst()
                .orElse(null);

        // 没找到权限列表
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }


    /**
     * 获取权限列表
     *
     * @param space
     * @param loginUser
     * @return
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }

        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());

        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }

        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            // 私有空间，仅本人或管理员有所有权限
            case PRIVATE:
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }

            // 团队空间，查询 SpaceUser 并获取角色和权限
            case TEAM:
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }

        }
        return new ArrayList<>();
    }
}