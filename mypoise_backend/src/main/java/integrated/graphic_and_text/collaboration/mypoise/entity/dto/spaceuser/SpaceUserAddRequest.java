package integrated.graphic_and_text.collaboration.mypoise.entity.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入空间请求
 */
@Data
public class SpaceUserAddRequest implements Serializable {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;

}
