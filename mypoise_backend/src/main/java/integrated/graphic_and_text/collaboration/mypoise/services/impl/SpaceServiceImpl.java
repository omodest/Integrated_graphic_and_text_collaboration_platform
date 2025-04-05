package integrated.graphic_and_text.collaboration.mypoise.services.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import integrated.graphic_and_text.collaboration.mypoise.constant.UserConstant;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.space.SpaceAddRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.space.SpaceQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.SpaceLevelEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.SpaceRoleEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.SpaceTypeEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Space;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.SpaceUser;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.User;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.SpaceVO;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.UserInfoVO;
import integrated.graphic_and_text.collaboration.mypoise.exception.BusinessException;
import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import integrated.graphic_and_text.collaboration.mypoise.exception.ThrowUtils;
import integrated.graphic_and_text.collaboration.mypoise.manage.sharding.DynamicShardingManager;
import integrated.graphic_and_text.collaboration.mypoise.services.SpaceService;
import integrated.graphic_and_text.collaboration.mypoise.mapper.SpaceMapper;
import integrated.graphic_and_text.collaboration.mypoise.services.SpaceUserService;
import integrated.graphic_and_text.collaboration.mypoise.services.UserService;
import integrated.graphic_and_text.collaboration.mypoise.utils.RedissonLockUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author poise
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-01-21 20:24:16
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private UserService userService;

    /**
     * 分布式锁
     */
    @Resource
    private RedissonLockUtils redissonLockUtils;

    /**
     * 事务
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 分库分表中间件
     */
//    @Lazy
//    @Resource
//    private DynamicShardingManager dynamicShardingManager;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1. 参数填充，什么参数都不没传，就属于普通用户创建空间的情况
        String spaceName = spaceAddRequest.getSpaceName();
        if (StrUtil.isEmpty(spaceName)){
            spaceAddRequest.setSpaceName("默认空间");
        }
        Integer spaceLevel = spaceAddRequest.getSpaceLevel();
        if (ObjectUtil.isEmpty(spaceLevel)){
            spaceAddRequest.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        Integer spaceType = spaceAddRequest.getSpaceType();
        if (spaceType == null){
            spaceAddRequest.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        // 2. 参数校验
        this.validSpace(space, true);
        // 3. 权限校验,非管理员默认只能创建普通的空间、以及一个团队空间
        ThrowUtils.throwIf(!userService.isAdmin(loginUser) && SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel(),
            ErrorCode.NO_AUTH_ERROR, "非管理员只能创建默认普通空间");
        // 4. 创建操作
        // 分布式锁操作，锁名为用户id加上一个字符串表示; 可以采用ConcurrentHashMap来存储锁对象，提升性能
        redissonLockUtils.redissonDistributedLocks(loginUser.getId() + "create", () -> {
            // 编程式事务管理器
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断是否已有空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, loginUser.getId())
                        .eq(Space::getSpaceType, spaceAddRequest.getSpaceType())
                        .exists();
                // 如果已有空间，就不能再创建
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间仅能创建一个");
                // 创建
                space.setUserId(loginUser.getId());
                space.setMaxCount(SpaceLevelEnum.COMMON.getMaxCount());
                space.setMaxSize(SpaceLevelEnum.COMMON.getMaxSize());
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间到数据库失败");
                // 如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(loginUser.getId());
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
                // 创建分表（仅对团队空间生效）为方便部署
//                dynamicShardingManager.createSpacePictureTable(space);
                // 返回新写入的数据 id
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        });
        return 0;
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 创建时校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR,  "空间级别不能为空");
            ThrowUtils.throwIf(spaceType == null, ErrorCode.PARAMS_ERROR,  "空间类别不能为空");
        }
        ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName) && spaceName.length() > 30,
                ErrorCode.PARAMS_ERROR,  "空间名称过长");
        ThrowUtils.throwIf(spaceLevel != null && spaceLevelEnum == null,
                ErrorCode.PARAMS_ERROR,  "空间级别不存在");
        // 修改数据时，空间类别进行校验
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不存在");
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserInfoVO userVO = userService.getUserVo(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        // 1. 参数校验
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 2. 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 3. . 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 4. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVo(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortFiled(); // 排序字段sortFlied
        String sortOrder = spaceQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);

        Integer spaceType = spaceQueryRequest.getSpaceType();
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId())
                && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "仅本人或者管理员可操作");
    }
}




