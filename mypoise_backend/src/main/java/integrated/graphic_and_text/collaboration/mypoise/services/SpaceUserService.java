package integrated.graphic_and_text.collaboration.mypoise.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.spaceuser.SpaceUserAddRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.dto.spaceuser.SpaceUserQueryRequest;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.SpaceUser;
import integrated.graphic_and_text.collaboration.mypoise.entity.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author poise
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-01-25 18:23:40
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);
    /**
     * 校验空间成员
     *
     * @param spaceUser
     * @param add       是否为创建时检验
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);
    /**
     * 获取空间成员包装类（单条）
     *
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);
    /**
     * 获取空间成员包装类（列表）
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

}
