package integrated.graphic_and_text.collaboration.mypoise.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    private int id;

    private static final long serialVersionUID = 1L;
}
