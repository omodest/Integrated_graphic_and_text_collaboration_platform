package integrated.graphic_and_text.collaboration.mypoise.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 生命周期-程序运行期间
@Target(ElementType.METHOD) // 作用范围-方法
public @interface AuthCheck {

    /**
     * 权限
     * @return
     */
    String mustRole() default "";
}
