package integrated.graphic_and_text.collaboration.mypoise.manage.sharding;


import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.SpaceLevelEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.enums.SpaceTypeEnum;
import integrated.graphic_and_text.collaboration.mypoise.entity.model.Space;
import integrated.graphic_and_text.collaboration.mypoise.services.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分库分表管理器，用于动态管理图片分表的配置和创建
 *
 * 1. 在系统启动时初始化动态分表配置。
 * 2. 获取当前存在的所有图片分表（包括默认逻辑表 picture 和根据 spaceId 命名的分表 picture_{spaceId}）。
 * 3. 更新 ShardingSphere 的 actual-data-nodes 配置，使其包含所有存在的表。
 * 4. 动态创建新的图片分表，并在创建后更新分表配置。
 * 5. 获取 ShardingSphere 的 ContextManager，用于动态调整分片规则。
 */
@Component
@Slf4j
public class DynamicShardingManager {

    // 数据源，用于获取数据库连接，执行 SQL 查询和操作
    @Resource
    private DataSource dataSource;

    @Resource
    private SpaceService spaceService;
    // 逻辑表名称，默认的图片表名
    private static final String LOGIC_TABLE_NAME = "picture";
    // 数据库名称，与配置文件中配置的数据库名称保持一致
    private static final String DATABASE_NAME = "logic_db";


    /**
     * 在 bean 初始化后执行，启动时调用，初始化动态分表配置。
     */
    @PostConstruct
    public void initialize() {
        log.info("初始化动态分表配置...");
        updateShardingTableNodes(); // 更新分表节点配置，确保 ShardingSphere 获取到最新的表信息
    }

    /**
     * 获取所有动态表名，包括初始表 picture 和分表 picture_{spaceId}
     * 过滤掉那些虽然理论上应该存在，但实际数据库中不存在的表（例如已经删除的表）。
     */
    private Set<String> fetchAllPictureTableNames() {
        log.info("获取所有实际存在的分表...");

        // 查询所有符合条件的空间，这里仅选取团队空间且为旗舰版
        Set<Long> spaceIds = spaceService.lambdaQuery()
                .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                .eq(Space::getSpaceLevel, SpaceLevelEnum.FLAGSHIP.getValue())
                .list()
                .stream()
                .map(Space::getId)
                .collect(Collectors.toSet());

        // 根据空间ID生成理论上的分表名称，并过滤掉数据库中不存在的表
        Set<String> tableNames = spaceIds.stream()
                .map(spaceId -> LOGIC_TABLE_NAME + "_" + spaceId)
//                .filter(this::isTableExists) // 动态检查表是否存在
                .collect(Collectors.toSet());
        tableNames.add(LOGIC_TABLE_NAME); // 添加初始逻辑表
        return tableNames;
    }

    /**
     * 更新 ShardingSphere 的 actual-data-nodes 动态表名配置
     *
     * 1. 获取所有实际存在的表名。
     * 2. 拼接成 ShardingSphere 所需的 actual-data-nodes 格式（数据库名.表名）。
     * 3. 获取 ShardingSphere 的 ContextManager，并从中获取当前的分片规则配置。
     * 4. 更新逻辑表对应的分片规则，替换 actual-data-nodes 配置。
     * 5. 调用 ContextManager 更新分片规则并重新加载数据库配置。
     */
    private void updateShardingTableNodes() {
        log.info("更新 ShardingSphere 分表节点配置...");
        // 获取所有当前存在的表名
        Set<String> tableNames = fetchAllPictureTableNames();
        // 拼接 actual-data-nodes 字符串，格式为 "cloud_library.表名1,cloud_library.表名2,..." 保证前缀合法
        String newActualDataNodes = tableNames.stream()
                .map(tableName -> "cloud_library." + tableName) // 确保前缀合法
                .collect(Collectors.joining(","));
        log.info("动态分表 actual-data-nodes 配置: {}", newActualDataNodes);

        // 获取 ShardingSphere 的 ContextManager 对象
        ContextManager contextManager = getContextManager();
        // 获取当前数据库的分片规则元数据
        ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts()
                .getMetaData()
                .getDatabases()
                .get(DATABASE_NAME)
                .getRuleMetaData();

        // 查找单个 ShardingRule 配置
        Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
        if (shardingRule.isPresent()) {
            // 获取当前的分片规则配置
            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();
            // 更新表的分片规则配置，只更新逻辑表名称为 "picture" 的规则
            List<ShardingTableRuleConfiguration> updatedRules = ruleConfig.getTables()
                    .stream()
                    .map(oldTableRule -> {
                        if (LOGIC_TABLE_NAME.equals(oldTableRule.getLogicTable())) {
                            // 构建新的表规则配置，替换 actual-data-nodes 为最新的配置
                            ShardingTableRuleConfiguration newTableRuleConfig = new ShardingTableRuleConfiguration(LOGIC_TABLE_NAME, newActualDataNodes);
                            newTableRuleConfig.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());
                            newTableRuleConfig.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());
                            newTableRuleConfig.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());
                            newTableRuleConfig.setAuditStrategy(oldTableRule.getAuditStrategy());
                            return newTableRuleConfig;
                        }
                        return oldTableRule;
                    })
                    .collect(Collectors.toList());
            // 更新配置中的所有表规则
            ruleConfig.setTables(updatedRules);
            contextManager.alterRuleConfiguration(DATABASE_NAME, Collections.singleton(ruleConfig));
            // 重新加载数据库配置，确保新的配置生效
            contextManager.reloadDatabase(DATABASE_NAME);
            log.info("动态分表规则更新成功！");
        } else {
            log.error("未找到 ShardingSphere 的分片规则配置，动态分表更新失败。");
        }
    }

    /**
     * 检查表是否实际存在
     *
     * 通过查询 information_schema.tables 系统表来判断：
     * - table_schema 为 'cloud_library'（数据库名）
     * - table_name 为传入的表名
     */
    private boolean isTableExists(String tableName) {
        // SQL 查询，用于统计符合条件的表数量
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'cloud_library' AND table_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // 设置 SQL 参数
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                // 如果查询结果存在且数量大于 0，则表存在
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("检查表存在性失败: {}", tableName, e);
            return false;
        }
    }

    /**
     * 动态创建空间图片分表
     *
     * 仅为符合条件的旗舰版团队空间创建分表，创建步骤如下：
     * 1. 根据空间 ID 拼接出新表名称（格式为 picture_{spaceId}）。
     * 2. 执行 SQL 语句，通过 LIKE 语法复制默认逻辑表 picture 的结构，
     *    从而创建新表。
     * 3. 创建完成后，调用 updateShardingTableNodes 方法更新分表配置，
     *    使新的表能在后续查询中被正确路由到。
     */
    public void createSpacePictureTable(Space space) {
        log.info("初始化动态分表配置3...");
        // 仅为旗舰版团队空间创建分表
        if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue() && space.getSpaceLevel() == SpaceLevelEnum.FLAGSHIP.getValue()) {
            Long spaceId = space.getId();
            // 根据空间 ID 拼接出新的表名，例如 "picture_123"
            String tableName = LOGIC_TABLE_NAME + "_" + spaceId;
            // 构造创建新表的 SQL 语句，使用 LIKE 从默认表复制表结构
            String createTableSql = "CREATE TABLE " + tableName + " LIKE " + LOGIC_TABLE_NAME;
            try {
                // 执行 SQL 语句创建新表
                SqlRunner.db().update(createTableSql);
                // 新表创建成功后，更新分表节点配置
                updateShardingTableNodes();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("创建图片空间分表失败，空间 id = {}", space.getId());
            }
        }
    }


    /**
     * 获取 ShardingSphere ContextManager
     *
     * 通过从数据源中获取连接，并将其转换为 ShardingSphereConnection，
     * 再调用 getContextManager() 方法来获取 ContextManager。
     */
    private ContextManager getContextManager() {
        log.info("初始化动态分表配置4...");
        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {
            // 从 ShardingSphereConnection 获取 ContextManager
            return connection.getContextManager();
        } catch (SQLException e) {
            throw new RuntimeException("获取 ShardingSphere ContextManager 失败", e);
        }
    }
}