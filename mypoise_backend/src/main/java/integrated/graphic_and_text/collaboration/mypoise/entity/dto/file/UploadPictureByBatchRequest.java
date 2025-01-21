package integrated.graphic_and_text.collaboration.mypoise.entity.dto.file;

import lombok.Data;


@Data
public class UploadPictureByBatchRequest {


    /**
     * 抓取数量
     */
    private Integer count = 10;

    /**
     * 抓取关键字
     */
    private String searchText;

    /**
     * 名称前缀
     */
    private String namePrefix;

}
