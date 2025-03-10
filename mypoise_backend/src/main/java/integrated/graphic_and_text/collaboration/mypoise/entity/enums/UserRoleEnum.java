package integrated.graphic_and_text.collaboration.mypoise.entity.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 用户权限枚举
 */
@Getter
public enum UserRoleEnum {

    USER("用户","user"),

    ADMIN("管理员","admin"),

    VIP("会员","vip"),

    FVIP("永久会员","fvip");

    private final String text;

    private final String value;

    /**
     * 构造函数（缺省-包级私有）
     * @param text
     * @param value
     */
    UserRoleEnum(String text, String value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据Value获取枚举值
     * @param value
     * @return
     */
    public static UserRoleEnum getEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (UserRoleEnum userRoleEnum: UserRoleEnum.values()){
            if (userRoleEnum.value.equals(value)){
                return userRoleEnum;
            }
        }
        return null;
    }
}
