use cloud_library;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    wechat_id   bigint  null comment '微信id',
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/vip/f_vip',
    vip_expire  datetime                      null comment '会员过期时间',
    email        varchar(40)                            null comment '用户qq邮箱',
    gender       char(2)      default '未知'           null comment '性别',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间-可由用户控制',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 用户-微信表
CREATE TABLE user_wechat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户微信表ID',

    openid VARCHAR(256) NOT NULL COMMENT '微信扫码登录的OpenID',  -- 用于微信扫码登录
    session_key VARCHAR(512) NULL COMMENT '微信扫码登录的SessionKey',  -- 用于微信扫码登录
    unionid VARCHAR(256) NOT NULL COMMENT '微信统一标识 UnionID',  -- 用于多个平台的统一标识（扩展性字段）

    mini_program_openid VARCHAR(256) NULL COMMENT '微信小程序的OpenID',  -- 用于小程序
    mini_program_unionid VARCHAR(256) NULL COMMENT '微信小程序的UnionID',  -- 用于小程序（扩展性字段）

    mini_program_bind_time DATETIME NULL COMMENT '微信小程序绑定时间',  -- 绑定时间

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uniq_openid (openid),  -- 为 openid 设置唯一索引
    UNIQUE KEY uniq_unionid (unionid)  -- 为 unionid 设置唯一索引
);

-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(256)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    categoryId    bigint                        null comment '分类ID',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (categoryId),         -- 提升基于分类的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

-- 标签表
create table if not exists picture_tags
(
    id            bigint auto_increment comment 'id' primary key,
    applyTotal  int     default 0 comment '标签使用数',
    tagName  varchar(50) comment '标签名称',
    userId bigint default 0 comment '创建者ID',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uniq_tag_name(tagName) -- 为 tagName 设置唯一索引

) comment '图片标签表' collate = utf8mb4_unicode_ci;

-- 图片-标签表
create table if not exists picture_tag_relation
(
    pictureId    bigint not null comment '图片 ID',
    tagId        bigint not null comment '标签 ID',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    primary key (pictureId, tagId) -- 联合主键，确保每个图片标签唯一组合
) comment '图片与标签关联表' collate = utf8mb4_unicode_ci;

-- 图片类别表
create table if not exists picture_category
(
    id            bigint auto_increment comment 'id' primary key,
    applyTotal  int     default 0 comment '类型使用数',
    categoryName  varchar(50) comment '类别名称',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uniq_category_name(categoryName) -- 为 categoryName 设置唯一索引
) comment '图片类别表' collate = utf8mb4_unicode_ci;

/**
  触发器 添加图片更新标签和类型使用数
 */
DELIMITER $$

CREATE TRIGGER update_applyTotal_after_insert_tags
    AFTER INSERT ON picture_tag_relation
    FOR EACH ROW
BEGIN
    -- 增加相应标签的 applyTotal
    UPDATE picture_tags
    SET applyTotal = applyTotal + 1
    WHERE id = NEW.tagId;
END $$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER update_applyTotal_after_increase_category
    AFTER INSERT ON picture
    FOR EACH ROW
BEGIN
    -- 增加相应标签的 applyTotal
    UPDATE picture_category
    SET applyTotal = applyTotal + 1
    WHERE id = NEW.categoryId;
END $$

DELIMITER ;

/**
  触发器 减少图片更新标签和类型使用数
 */
DELIMITER $$

CREATE TRIGGER update_applyTotal_after_delete_tags
    AFTER DELETE ON picture_tag_relation
    FOR EACH ROW
BEGIN
    -- 减少相应标签的 applyTotal
    UPDATE picture_tags
    SET applyTotal = applyTotal - 1
    WHERE id = OLD.tagId;
END $$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER update_applyTotal_after_delete_category
    AFTER INSERT ON picture
    FOR EACH ROW
BEGIN
    -- 增加相应标签的 applyTotal
    UPDATE picture_category
    SET applyTotal = applyTotal - 1
    WHERE id = NEW.categoryId;
END $$

-- 审核表