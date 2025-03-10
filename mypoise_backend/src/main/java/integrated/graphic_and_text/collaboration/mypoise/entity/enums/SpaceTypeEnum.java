package integrated.graphic_and_text.collaboration.mypoise.entity.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 空间类型枚举
 */
@Getter
public enum SpaceTypeEnum {

    PRIVATE("私人空间", 0),

    TEAM("团队空间", 1);

    private final String text;

    private final int value;

    SpaceTypeEnum(String text, int value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value创建枚举
     * @param value
     * @return
     */
    public static SpaceTypeEnum getEnumByValue(Integer value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum: SpaceTypeEnum.values()){
            if (spaceTypeEnum.value == value){
                return spaceTypeEnum;
            }
        }
        return null;
    }
}
