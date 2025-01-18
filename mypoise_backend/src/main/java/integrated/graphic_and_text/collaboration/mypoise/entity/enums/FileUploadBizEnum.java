package integrated.graphic_and_text.collaboration.mypoise.entity.enums;


import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 文件上传业务类型枚举
 */
@Getter
public enum FileUploadBizEnum {

    USER_AVATAR("用户头像", "user_avatar"),

    PUBLIC_RESOURCE("'公开图片资源","public_resource");

    private final String text;

    private final String value;

    FileUploadBizEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static FileUploadBizEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FileUploadBizEnum anEnum : FileUploadBizEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}