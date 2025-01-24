package integrated.graphic_and_text.collaboration.mypoise.entity.vo.spaceAnalyze;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间分类响应类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpaceCategoryAnalyzeResponse implements Serializable {

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片分类总大小
     */
    private Long totalSize;

    /**
     * 图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
