package integrated.graphic_and_text.collaboration.mypoise;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan("integrated.graphic_and_text.collaboration.mypoise.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 如果在系统中需要 访问对象的代理对象，可以开启这个注解(默认是关闭的)
@SpringBootApplication
@EnableAsync
public class MypoiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MypoiseApplication.class, args);
    }

}
