package integrated.graphic_and_text.collaboration.mypoise.constant;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public interface CacheConstant {

    /**
     * 本地缓存-hashmap创建
     */
    Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 热门标签缓存
     */
    String HOT_TAGS =  "hot_tags";

}
