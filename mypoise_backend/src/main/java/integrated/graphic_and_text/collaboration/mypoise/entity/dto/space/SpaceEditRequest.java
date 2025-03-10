package integrated.graphic_and_text.collaboration.mypoise.entity.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 提供给用户使用的编辑，仅支持用户修改空间名称
 */
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
