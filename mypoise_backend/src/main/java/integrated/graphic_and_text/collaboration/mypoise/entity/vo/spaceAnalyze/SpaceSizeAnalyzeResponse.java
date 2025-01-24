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
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 图片大小范围
     */
    private String sizeRange;
    /**
     * 图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
