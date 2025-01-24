package integrated.graphic_and_text.collaboration.mypoise.entity.vo.spaceAnalyze;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间标签响应类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpaceTagsAnalyzeResponse implements Serializable {

    /**
     * 图片分类
     */
    private String tag;

    /**
     * 使用数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
