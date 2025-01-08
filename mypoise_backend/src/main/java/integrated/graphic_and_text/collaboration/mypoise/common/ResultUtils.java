package integrated.graphic_and_text.collaboration.mypoise.common;

import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;

public class ResultUtils {

    /**
     * <T> 泛型使用需要这个关键字，BaseResponse<T> 返回值
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0, data,"ok");
    }

    /**
     * 这里需要不使用泛型T，所以BaseResponse前不需要<T>声明，后面也只需要使用一个占位符
     */
    public static BaseResponse<?> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse<?> error(int code, String message){
        return new BaseResponse<>(code, "", message);
    }

    public static BaseResponse<?> error(ErrorCode errorCode, String message){
        return new BaseResponse<>(errorCode.getCode(), "", message);
    }

}
