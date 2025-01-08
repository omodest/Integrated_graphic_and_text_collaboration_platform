package integrated.graphic_and_text.collaboration.mypoise.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest{

    /**
     * 页数
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortFiled;

    /**
     * 默认排序方式-降序
     */
    private String sortOrder = "desc";
}
