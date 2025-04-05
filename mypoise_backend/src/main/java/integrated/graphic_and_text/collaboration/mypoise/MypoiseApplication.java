package integrated.graphic_and_text.collaboration.mypoise;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("integrated.graphic_and_text.collaboration.mypoise.mapper") // 数据库扫描的mapper类
@EnableAspectJAutoProxy(exposeProxy = true) // 如果在系统中需要 访问对象的代理对象，可以开启这个注解(默认是关闭的)
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@EnableAsync
@EnableScheduling // 开启定时任务
public class MypoiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MypoiseApplication.class, args);
    }

}
