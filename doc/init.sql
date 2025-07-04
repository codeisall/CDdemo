-- 创建数据库
CREATE DATABASE IF NOT EXISTS `energy_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `energy_db`;

-- 用户表
CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                         `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
                         `unionid` VARCHAR(64) NULL COMMENT '微信unionid',
                         `nickname` VARCHAR(64) NULL COMMENT '用户昵称',
                         `avatar_url` VARCHAR(255) NULL COMMENT '用户头像',
                         `phone` VARCHAR(16) NULL COMMENT '手机号',
                         `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
                         `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1-正常，0-禁用',
                         `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_openid` (`openid`),
                         KEY `idx_phone` (`phone`),
                         KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 充电站表（基于现有StationInfo扩展）
CREATE TABLE `charging_stations` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `station_id` VARCHAR(32) NOT NULL COMMENT '充电站ID',
                                     `station_name` VARCHAR(128) NOT NULL COMMENT '充电站名称',
                                     `address` VARCHAR(255) NULL COMMENT '详细地址',
                                     `province` VARCHAR(32) NULL COMMENT '省份',
                                     `city` VARCHAR(32) NULL COMMENT '城市',
                                     `district` VARCHAR(32) NULL COMMENT '区县',
                                     `longitude` DECIMAL(10,7) NULL COMMENT '经度',
                                     `latitude` DECIMAL(10,7) NULL COMMENT '纬度',
                                     `station_tel` VARCHAR(32) NULL COMMENT '联系电话',
                                     `service_fee` DECIMAL(8,4) NULL COMMENT '服务费',
                                     `parking_fee` VARCHAR(128) NULL COMMENT '停车费说明',
                                     `opening_hours` VARCHAR(128) NULL COMMENT '营业时间',
                                     `station_status` TINYINT NOT NULL DEFAULT 2 COMMENT '充电站状态：0-未知，1-建设中，2-运营中，3-关闭下线',
                                     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_station_id` (`station_id`),
                                     KEY `idx_location` (`province`, `city`, `district`),
                                     KEY `idx_status` (`station_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电站表';

-- 充电桩表（基于现有ConnectorInfo扩展）
CREATE TABLE `charging_connectors` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `connector_id` VARCHAR(32) NOT NULL COMMENT '充电桩ID',
                                       `station_id` VARCHAR(32) NOT NULL COMMENT '所属充电站ID',
                                       `connector_name` VARCHAR(128) NULL COMMENT '充电桩名称',
                                       `connector_type` TINYINT NOT NULL COMMENT '充电枪类型：1-直流，2-交流',
                                       `rated_power` DECIMAL(8,2) NULL COMMENT '额定功率(kW)',
                                       `current_power` DECIMAL(8,2) NULL DEFAULT 0.00 COMMENT '当前功率(kW)',
                                       `electricity_fee` DECIMAL(8,4) NULL COMMENT '电费单价(元/度)',
                                       `service_fee` DECIMAL(8,4) NULL COMMENT '服务费单价(元/度)',
                                       `status` TINYINT NOT NULL DEFAULT 1 COMMENT '充电桩状态：0-离网，1-空闲，2-占用（充电中），3-占用（未充电），4-预约，255-故障',
                                       `status_update_time` DATETIME NULL COMMENT '状态更新时间',
                                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_connector_id` (`connector_id`),
                                       KEY `idx_station_id` (`station_id`),
                                       KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电桩表';

-- 充电订单表
CREATE TABLE `charge_orders` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                                 `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                 `connector_id` VARCHAR(32) NOT NULL COMMENT '充电桩ID',
                                 `station_id` VARCHAR(32) NOT NULL COMMENT '充电站ID',
                                 `platform_order_no` VARCHAR(32) NULL COMMENT '平台订单号（对接电能平台的订单号）',
                                 `start_time` DATETIME NULL COMMENT '开始充电时间',
                                 `end_time` DATETIME NULL COMMENT '结束充电时间',
                                 `total_power` DECIMAL(10,3) NULL DEFAULT 0.000 COMMENT '累计充电量(度)',
                                 `total_fee` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '总费用(元)',
                                 `electricity_fee` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '电费(元)',
                                 `service_fee` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '服务费(元)',
                                 `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1-待充电，2-充电中，3-充电完成，4-已结算，5-已取消，6-异常',
                                 `charge_status` TINYINT NULL COMMENT '充电状态：1-启动中，2-充电中，3-停止中，4-已结束，5-未知',
                                 `stop_reason` TINYINT NULL COMMENT '停止原因：0-用户手动停止，1-平台停止，2-BMS停止，3-设备故障，4-连接器断开',
                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_order_no` (`order_no`),
                                 UNIQUE KEY `uk_platform_order_no` (`platform_order_no`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_connector_id` (`connector_id`),
                                 KEY `idx_status` (`status`),
                                 KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电订单表';

-- 支付订单表
CREATE TABLE `payment_orders` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付订单ID',
                                  `order_no` VARCHAR(32) NOT NULL COMMENT '支付订单号',
                                  `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                  `relate_order_no` VARCHAR(32) NULL COMMENT '关联订单号（充电订单号）',
                                  `wx_order_no` VARCHAR(64) NULL COMMENT '微信支付订单号',
                                  `wx_transaction_id` VARCHAR(64) NULL COMMENT '微信支付交易号',
                                  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额(元)',
                                  `type` TINYINT NOT NULL COMMENT '支付类型：1-充值，2-充电消费',
                                  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '支付状态：1-待支付，2-支付成功，3-支付失败，4-已退款',
                                  `pay_time` DATETIME NULL COMMENT '支付时间',
                                  `refund_time` DATETIME NULL COMMENT '退款时间',
                                  `refund_amount` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '退款金额(元)',
                                  `remark` VARCHAR(255) NULL COMMENT '备注',
                                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_order_no` (`order_no`),
                                  UNIQUE KEY `uk_wx_order_no` (`wx_order_no`),
                                  KEY `idx_user_id` (`user_id`),
                                  KEY `idx_type_status` (`type`, `status`),
                                  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';

-- 用户余额流水表
CREATE TABLE `balance_records` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
                                   `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                   `order_no` VARCHAR(32) NULL COMMENT '关联订单号',
                                   `amount` DECIMAL(10,2) NOT NULL COMMENT '变动金额(元)，正数为增加，负数为减少',
                                   `balance_before` DECIMAL(10,2) NOT NULL COMMENT '变动前余额(元)',
                                   `balance_after` DECIMAL(10,2) NOT NULL COMMENT '变动后余额(元)',
                                   `type` TINYINT NOT NULL COMMENT '流水类型：1-充值，2-充电消费，3-退款',
                                   `description` VARCHAR(255) NULL COMMENT '描述',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_user_id` (`user_id`),
                                   KEY `idx_order_no` (`order_no`),
                                   KEY `idx_type` (`type`),
                                   KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户余额流水表';

-- 充电明细表（记录分时段的充电详情）
CREATE TABLE `charge_details` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
                                  `charge_order_id` BIGINT NOT NULL COMMENT '充电订单ID',
                                  `start_time` DATETIME NOT NULL COMMENT '时段开始时间',
                                  `end_time` DATETIME NULL COMMENT '时段结束时间',
                                  `electricity_price` DECIMAL(8,4) NOT NULL COMMENT '时段电价(元/度)',
                                  `service_price` DECIMAL(8,4) NOT NULL COMMENT '时段服务费(元/度)',
                                  `power` DECIMAL(10,3) NULL DEFAULT 0.000 COMMENT '时段充电量(度)',
                                  `electricity_fee` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '时段电费(元)',
                                  `service_fee` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '时段服务费(元)',
                                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_charge_order_id` (`charge_order_id`),
                                  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电明细表';