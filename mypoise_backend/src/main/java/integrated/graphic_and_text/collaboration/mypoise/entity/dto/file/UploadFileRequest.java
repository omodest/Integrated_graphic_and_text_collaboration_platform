package integrated.graphic_and_text.collaboration.mypoise.entity.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 */
// region DATA
// 可以生成 toString equals hashCode 全参构造函数 无参构造函数
// @NonNull：可以与字段一起使用，自动为字段生成非空检查。
// endregion
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务
     */
    private String biz;

    // region Serializable
    // 1. 实现序列化接口目的: 使POJO对象能被序列化，方便在网络上传输、状态保存、深拷贝、持久化等；

    // 2. 非序列化字段：transient 关键字用于排除某些字段的序列化，这样可以控制哪些状态需要被保存(比如一些敏感信息)；

    // 3. 序列化ID作用：版本控制当一个类被序列化后，如果类的结构发生了变化，例如 添加字段，在反序列化时，没有正确的 serialVersionUID，会抛出 InvalidClassException。
    // 且如果没有定义 serialVersionUID，Java 会在运行时根据类的结构自动生成一个值，这个值可能会因小的变化而不同（例如字段的顺序、字段类型等）。
    // 这会导致在不同版本之间进行序列化和反序列化时产生不必要的错误。
    // endregion
    private static final long serialVersionUID = 1L;
}