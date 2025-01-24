package integrated.graphic_and_text.collaboration.mypoise.entity.dto.space.analyze;

import integrated.graphic_and_text.collaboration.mypoise.entity.dto.space.analyze.base.SpaceAnalyzeRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间用户上传行为分析请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {
    /**
     * 用户 ID
     */
    private Long userId;
    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;
}
