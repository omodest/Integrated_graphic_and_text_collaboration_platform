package integrated.graphic_and_text.collaboration.mypoise.entity.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 图片审核枚举
 */
@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("待审核", 0),

    PASS("通过", 1),

    REJECT("拒绝", 2);

    private final String text;

    private final int value;

    PictureReviewStatusEnum(String text, int value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据传来的枚举状态值，拿到对应的枚举对象
     * @param value
     * @return
     */
    public static PictureReviewStatusEnum getEnumByValue(int value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum: PictureReviewStatusEnum.values()){
            if (pictureReviewStatusEnum.getValue() == value){
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
