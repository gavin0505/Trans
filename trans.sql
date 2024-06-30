drop database if exists trans;
create database trans CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
use trans;

DROP TABLE IF EXISTS url_map;
CREATE TABLE url_map
(
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `short_url`        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '短链URL',
    `long_url`         VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '长链URL',
    `compression_code` VARCHAR(16)   NOT NULL DEFAULT '' COMMENT '压缩码',
    `biz_type`         VARCHAR(8)    NOT NULL DEFAULT 'v' COMMENT '业务类别标识',
    `domain_conf_id`   BIGINT        NOT NULL DEFAULT 0 COMMENT '域名配置信息id',
    `description`      VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '描述',
    `expire_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
    `url_status`       TINYINT       NOT NULL DEFAULT 1 COMMENT 'URL状态,1:正常,2:已失效',
    `user_id`          BIGINT        NOT NULL DEFAULT 0 COMMENT '用户Id',
    `creator`          INT(11)       NOT NULL DEFAULT 1 COMMENT '创建者',
    `editor`           INT(11)       NOT NULL DEFAULT 1 COMMENT '更新者',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `edit_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`          BIGINT        NOT NULL DEFAULT 1 COMMENT '版本号',
    `deleted`          TINYINT       NOT NULL DEFAULT 0 COMMENT '软删除标志',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = 'URL映射表';


CREATE INDEX idx_compression_code ON url_map (compression_code);
CREATE INDEX idx_short_url ON url_map (short_url);
CREATE INDEX idx_expire_time ON url_map (expire_time);
CREATE INDEX idx_user_id ON url_map (user_id);
CREATE INDEX idx_description ON url_map (description);

DROP TABLE IF EXISTS compression_code;
CREATE TABLE compression_code
(
    `id`               BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `compression_code` VARCHAR(16)     NOT NULL DEFAULT '' COMMENT '压缩码',
    `code_status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '压缩码状态,1:未使用,2:已使用,3:已失效',
    `domain_conf_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '域名配置主键',
    `creator`          INT(11)         NOT NULL DEFAULT 1 COMMENT '创建者',
    `editor`           INT(11)         NOT NULL DEFAULT 1 COMMENT '更新者',
    `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `edit_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`          BIGINT          NOT NULL DEFAULT 1 COMMENT '版本号',
    `deleted`          TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标识',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '压缩表';

CREATE UNIQUE INDEX uniq_compression_code ON compression_code (compression_code, domain_conf_id);

DROP TABLE IF EXISTS domain_conf;
CREATE TABLE domain_conf
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `domain_value`  VARCHAR(16)     NOT NULL DEFAULT '' COMMENT '域名',
    `protocol`      VARCHAR(8)      NOT NULL DEFAULT 'https' COMMENT '协议',
    `biz_type`      VARCHAR(8)      NOT NULL DEFAULT 'v' COMMENT '业务类别标识',
    `domain_status` TINYINT         NOT NULL DEFAULT 1 COMMENT '域名状态，1：正常，2：失效',
    `creator`       INT(11)         NOT NULL DEFAULT 1 COMMENT '创建者',
    `editor`        INT(11)         NOT NULL DEFAULT 1 COMMENT '更新者',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `edit_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`       BIGINT          NOT NULL DEFAULT 1 COMMENT '版本号',
    `deleted`       TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标识',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '域名配置';

insert into domain_conf(`domain_value`, `biz_type`)
VALUES ('4im.cc', 'v'),
       ('4im.cc', 'j'),
       ('4im.cc', 'a');
insert into domain_conf(`domain_value`, `protocol`, `biz_type`)
VALUES ('localhost:8420', 'http', 'v'),
       ('localhost:8420', 'http', 'j');
;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE `sys_user`
(
    `id`          int(11)      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    varchar(128) NOT NULL DEFAULT '' COMMENT '用户名称',
    `nickname`    varchar(128) NOT NULL DEFAULT '' COMMENT '昵称',
    `phone`       varchar(15)  NOT NULL DEFAULT '' COMMENT '手机号',
    `email`       VARCHAR(128) NOT NULL DEFAULT '' COMMENT '电子邮箱',
    `password`    varchar(128) NOT NULL DEFAULT '' COMMENT '密码',
    `salt`        varchar(32)  NOT NULL DEFAULT '' COMMENT '盐',
    `logged`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后登录时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `edit_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`     BIGINT       NOT NULL DEFAULT 1 COMMENT '版本号',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '系统用户表';

# insert into sys_user(`username`, `email`, `password`) value ('Gavin', 'zxb_worky@163.com', '9fa6dca5b722d2560bb69047497ac4889cb99630913c7da169b46cadba249477');

DROP TABLE IF EXISTS sys_role;
CREATE TABLE `sys_role`
(
    `id`          int(11)      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        varchar(128) NOT NULL DEFAULT '' COMMENT '名称',
    `code`        varchar(128) NOT NULL DEFAULT '' COMMENT 'Key值',
    `remark`      varchar(128) NOT NULL DEFAULT '' COMMENT '角色描述',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `edit_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`     BIGINT       NOT NULL DEFAULT 1 COMMENT '版本号',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '角色表';

INSERT INTO `sys_role`(`name`, `code`, `remark`)
VALUES ('超级管理员', 'super-admin', '系统最高权限者'),
       ('管理员', 'admin', '系统管理者'),
       ('普通用户', 'user', '普通用户');


DROP TABLE IF EXISTS sys_user_role_mapping;
CREATE TABLE `sys_user_role_mapping`
(
    `id`          int(11)  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sys_user_id`     int(11)  NOT NULL DEFAULT 0 COMMENT '用户编号',
    `sys_role_id`     int(11)  NOT NULL DEFAULT 0 COMMENT '角色编号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `edit_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`     BIGINT   NOT NULL DEFAULT 1 COMMENT '版本号',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '软删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '用户-角色关系表';

# insert into sys_user_role_mapping(`user_id`, `role_id`) value (1, 3);
