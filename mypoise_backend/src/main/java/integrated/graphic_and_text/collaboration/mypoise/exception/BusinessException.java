package integrated.graphic_and_text.collaboration.mypoise.exception;

import lombok.Getter;

/**
 * 自定义异常类
 * 试想 直接抛Runtime异常，就无法直接从异常定位到问题，所以这里包装一下。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 状态码
     */
    private final int code;

    public BusinessException(int code, String message){
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode){
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message){
        super(message);
        this.code = errorCode.getCode();
    }
}
