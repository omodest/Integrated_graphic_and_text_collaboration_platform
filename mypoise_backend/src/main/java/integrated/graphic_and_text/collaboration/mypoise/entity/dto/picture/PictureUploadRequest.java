package integrated.graphic_and_text.collaboration.mypoise.entity.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传
 * 图片需要支持重复上传（基础信息不变，只改变图片文件），所以要添加图片 id 参数
 */
@Data
public class PictureUploadRequest implements Serializable {

    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;


    private static final long serialVersionUID = 1L;
}
