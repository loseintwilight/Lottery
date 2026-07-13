-- 创建数据库
CREATE DATABASE IF NOT EXISTS lottery_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lottery_db;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` INT AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(64) NOT NULL COMMENT '密码(SHA-256)',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 开奖表
DROP TABLE IF EXISTS `lottery_draw`;
CREATE TABLE `lottery_draw` (
    `id` INT AUTO_INCREMENT COMMENT '记录ID',
    `draw_no` VARCHAR(20) NOT NULL COMMENT '期号(如2026001)',
    `numbers` VARCHAR(20) DEFAULT NULL COMMENT '中奖号码(逗号分隔)',
    `draw_time` DATETIME DEFAULT NULL COMMENT '开奖时间',
    `total_bets` INT NOT NULL DEFAULT 0 COMMENT '当期总投注数',
    `total_amount` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '当期总金额',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_draw_no` (`draw_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开奖表';

-- 彩票表
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
    `id` BIGINT AUTO_INCREMENT COMMENT '记录ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `draw_no` VARCHAR(20) NOT NULL COMMENT '期号',
    `numbers` VARCHAR(20) NOT NULL COMMENT '所选号码(逗号分隔)',
    `bet_count` INT NOT NULL DEFAULT 1 COMMENT '投注倍数',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '花费金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-未开奖 1-未中奖 2-特等奖 3-一等奖',
    `buy_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_draw_no` (`draw_no`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='彩票表';

-- 中奖通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT AUTO_INCREMENT COMMENT '记录ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `draw_no` VARCHAR(20) NOT NULL COMMENT '期号',
    `ticket_id` BIGINT NOT NULL COMMENT '关联彩票ID',
    `prize_level` VARCHAR(20) NOT NULL COMMENT '奖级(特等奖/一等奖)',
    `prize_amount` DECIMAL(12,2) NOT NULL COMMENT '奖金',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '0-未读 1-已读',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_draw_no` (`draw_no`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中奖通知表';