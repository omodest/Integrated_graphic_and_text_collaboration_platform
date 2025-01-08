package integrated.graphic_and_text.collaboration.mypoise.exception;

import integrated.graphic_and_text.collaboration.mypoise.common.BaseResponse;
import integrated.graphic_and_text.collaboration.mypoise.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 全局的异常处理类，用于集中处理所有控制器层抛出的异常。
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class) // 处理指定类型的异常
    public BaseResponse<?> businessException(BusinessException businessException){
        log.error("BusinessException", businessException);
        return ResultUtils.error(businessException.getCode(), businessException.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeException(RuntimeException runtimeException){
        log.error("RuntimeException", runtimeException);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
