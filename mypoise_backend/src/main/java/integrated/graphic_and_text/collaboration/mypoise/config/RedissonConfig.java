package integrated.graphic_and_text.collaboration.mypoise.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private Integer database;

    private String host;

    private Integer port;

    // redis默认没有密码; 如果自己没有设置的话可以不用写
//    private String password;

    /**
     * 使用bean注解，spring启动时，会自动创建一个 Redissonclient对象
     * @return
     */
    @Bean
    public RedissonClient getRedissonClient(){
        // 1. 创建redisson配置对象
        Config config = new Config();
        // 创建单机Redisson，
        config.useSingleServer()
                .setDatabase(database) // 设置数据库，这里的set方法就是靠的lombok注解
                .setAddress("redis://" + host + ":" + port); // 设置端口
        // 创建Redisson实例
        return Redisson.create(config);
    }

}
