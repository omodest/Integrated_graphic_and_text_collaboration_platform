package integrated.graphic_and_text.collaboration.mypoise.common;

import integrated.graphic_and_text.collaboration.mypoise.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 封装通用返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    /**
     * 状态码
     */
    private int code;

    /**
     * 返回信息
     */
    private T data;

    /**
     * 返回提示信息
     */
    private String message;

    public BaseResponse(int code, T data, String message){
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data){
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

}
