package integrated.graphic_and_text.collaboration.mypoise.entity.dto.categoryTag;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryEditRequest implements Serializable {

    private long categoryId;

    private String categoryName;
}
