package integrated.graphic_and_text.collaboration.mypoise.job;

import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureTags;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureTagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class HotTagScheduler {

    @Resource
    private PictureTagsService pictureTagsService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void refreshHotTags() {
        // 1. 查询热门标签
        List<PictureTags> topTags = pictureTagsService.query()
                .orderByDesc("applyTotal")
                .last("LIMIT 5")
                .list();
        log.info("添加热门标签到redis");
        // 2. 更新到缓存
        redisTemplate.opsForValue().set("hot_tags", topTags, 3600); // 设置缓存有效期为1小时
    }
}
