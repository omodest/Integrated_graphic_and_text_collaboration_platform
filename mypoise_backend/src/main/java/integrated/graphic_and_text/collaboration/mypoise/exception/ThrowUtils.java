package integrated.graphic_and_text.collaboration.mypoise.exception;

/**
 * 异常抛出工具类(类似断言)
 */
public class ThrowUtils {

    /**
     *
     * @param condition 条件
     * @param exception 这里的RuntimeException是BusinessException的父类,所以可以使用子类作为实参(多态使用); 反过来就需要强制类型转换
     */
    public static void throwIf(boolean condition, RuntimeException exception){
        if (condition){
            throw exception;
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode){
        throwIf(condition, new BusinessException(errorCode));
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message){
        throwIf(condition, new BusinessException(errorCode, message));
    }

}
