package integrated.graphic_and_text.collaboration.mypoise.manage.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * 图片分表算法（该类直接在yml配置文件中，通过algorithmClassName引入使用）
 */
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    /**
     * 自定义的分表逻辑，精确分表算法，根据具体的分片键值决定数据存储在哪个表中。
     * @param availableTargetNames 当前可用的目标表名称集合。
     * @param preciseShardingValue 分片键值对象，包含逻辑表名与分片键的具体值。
     * @return  返回对应的实际表名；如果找不到对应表，则返回逻辑表名。
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue();
        String logicTableName = preciseShardingValue.getLogicTableName();
        // 如果分片键值为 null，表示不指定具体空间，可能为查询所有图片的情况
        if (spaceId == null) {
            return logicTableName;
        }
        // 根据 spaceId 动态生成分表名
        String realTableName = "picture_" + spaceId;
        // 判断生成的实际表名是否存在于当前可用的目标表集合中
        if (availableTargetNames.contains(realTableName)) {
            return realTableName;
        } else {
            return logicTableName;
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return new ArrayList<>();
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {
    }
}