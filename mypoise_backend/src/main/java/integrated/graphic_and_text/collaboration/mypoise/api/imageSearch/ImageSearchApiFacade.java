package integrated.graphic_and_text.collaboration.mypoise.api.imageSearch;

import integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.model.ImageSearchResult;
import integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.sub.GetImageFirstUrlApi;
import integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.sub.GetImageListApi;
import integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 门面设计模式
 * 门面模式通过提供一个统一的接口来简化多个接口的调用，使得客户端不需要关注内部的具体实现。
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        return GetImageListApi.getImageList(imageFirstUrl);
    }
}

