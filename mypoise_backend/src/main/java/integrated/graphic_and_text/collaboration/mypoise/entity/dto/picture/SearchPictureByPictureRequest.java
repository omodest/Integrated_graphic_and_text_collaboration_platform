package integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 根据这个图片id获取图片url
     */
    private Long pictureId;
}
