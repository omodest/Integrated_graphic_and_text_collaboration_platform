package integrated.graphic_and_text.collaboration.mypoise.entity.dto.space.analyze;

import integrated.graphic_and_text.collaboration.mypoise.entity.dto.space.analyze.base.SpaceAnalyzeRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间使用排名分析
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceRankAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 排名数
     */
    private final Integer topN = 10;


    private static final long serialVersionUID = 1L;
}
