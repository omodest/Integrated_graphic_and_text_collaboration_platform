package integrated.graphic_and_text.collaboration.mypoise.manage.auth;

import integrated.graphic_and_text.collaboration.mypoise.entity.model.Picture;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Space;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.SpaceUser;
import lombok.Data;

/**
 * SpaceUserAuthContext
 *
 * 表示用户在特定空间内的授权上下文，包括关联的图片、空间和用户信息(所以用户、图片、空间必要的消息)。
*/
@Data
public class SpaceUserAuthContext {

    /**
     * 临时参数，不同请求对应的 id 可能不同
     */
    private Long id;

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 空间用户 ID
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;

}