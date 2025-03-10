package integrated.graphic_and_text.collaboration.mypoise.entity.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 空间等级枚举
 */
@Getter
public enum SpaceLevelEnum {

    COMMON("普通版", 0, 100, 100L * 1024 * 1024),

    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),

    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String text;

    private final int value;

    private final long maxCount;

    private final long maxSize;

    SpaceLevelEnum(String text, int value, long maxCount, long maxSize){
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    public static SpaceLevelEnum getEnumByValue(Integer value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum: SpaceLevelEnum.values()){
            if (spaceLevelEnum.value == value){
                return spaceLevelEnum;
            }
        }
        return null;
    }
}