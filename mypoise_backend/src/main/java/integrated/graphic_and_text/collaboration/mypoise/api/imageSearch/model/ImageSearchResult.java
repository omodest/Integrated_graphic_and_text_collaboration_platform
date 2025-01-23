package integrated.graphic_and_text.collaboration.mypoise.api.imageSearch.model;


import lombok.Data;

/**
 * 以图搜图数据模型
 */
@Data
public class ImageSearchResult {

    /**
     * 来源地址
     */
    private String fromUrl;

    /**
     * 缩略图地址
     */
    private String thumbUrl;
}
