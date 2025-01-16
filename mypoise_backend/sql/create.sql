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

