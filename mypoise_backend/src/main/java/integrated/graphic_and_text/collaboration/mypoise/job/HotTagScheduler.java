package integrated.graphic_and_text.collaboration.mypoise.job;

import integrated.graphic_and_text.collaboration.mypoise.entity.model.PictureTags;
import integrated.graphic_and_text.collaboration.mypoise.services.PictureTagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static integrated.graphic_and_text.collaboration.mypoise.constant.CacheConstant.HOT_TAGS;

@Slf4j
@Component
public class HotTagScheduler {

    @Resource
    private PictureTagsService pictureTagsService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0/5 * * * * ?")
    public void refreshHotTags() {
        // 1. 查询热门标签
        List<PictureTags> topTags = pictureTagsService.query()
                .orderByDesc("applyTotal")
                .last("LIMIT 3")
                .list();
        log.info("添加热门标签到redis");

        String topTagNames = topTags.stream().map(PictureTags::getTagName).collect(Collectors.toList()).toString();
        // 2. 更新到缓存
        stringRedisTemplate.opsForValue().set(HOT_TAGS, topTagNames, 60, TimeUnit.SECONDS); // 设置缓存有效期为1分钟
    }
}
