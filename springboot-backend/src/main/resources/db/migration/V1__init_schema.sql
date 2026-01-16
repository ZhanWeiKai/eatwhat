-- 创建数据库
CREATE DATABASE IF NOT EXISTS what2eat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE what2eat;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    user_id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 菜品表
CREATE TABLE IF NOT EXISTS dish (
    dish_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(20) NOT NULL,
    image_url VARCHAR(255),
    uploader_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_uploader (uploader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 推送记录表
CREATE TABLE IF NOT EXISTS push (
    push_id VARCHAR(64) PRIMARY KEY,
    pusher_id VARCHAR(64) NOT NULL,
    pusher_name VARCHAR(50) NOT NULL,
    pusher_avatar VARCHAR(255),
    dishes JSON NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pusher (pusher_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 好友关系表
CREATE TABLE IF NOT EXISTS friendship (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    friend_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_friendship (user_id, friend_id),
    INDEX idx_user (user_id),
    INDEX idx_friend (friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试数据
-- 测试用户（密码都是：123456）
INSERT INTO user (user_id, username, password, nickname) VALUES
('user001', 'testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户1'),
('user002', 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户2'),
('user003', 'testuser3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户3')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 测试菜品
INSERT INTO dish (dish_id, name, description, price, category, uploader_id) VALUES
('dish001', '麻婆豆腐', '经典川菜，麻辣鲜香', 28.00, '热菜', 'user001'),
('dish002', '宫保鸡丁', '酸甜微辣，口感丰富', 38.00, '热菜', 'user001'),
('dish003', '红烧肉', '肥而不腻，入口即化', 45.00, '热菜', 'user002'),
('dish004', '糖醋排骨', '酸甜可口，老少皆宜', 42.00, '热菜', 'user002'),
('dish005', '清炒时蔬', '清爽健康，营养均衡', 18.00, '凉菜', 'user003'),
('dish006', '口水鸡', '麻辣鲜香，开胃下饭', 32.00, '凉菜', 'user003'),
('dish007', '扬州炒饭', '粒粒分明，配料丰富', 25.00, '主食', 'user001'),
('dish008', '牛肉面', '汤鲜味美，面条劲道', 28.00, '主食', 'user001'),
('dish009', '西红柿鸡蛋汤', '酸甜开胃，营养丰富', 15.00, '汤品', 'user002'),
('dish010', '银耳莲子汤', '清甜滋润，美容养颜', 18.00, '汤品', 'user002')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 测试推送
INSERT INTO push (push_id, pusher_id, pusher_name, dishes, total_amount) VALUES
('push001', 'user001', '测试用户1',
 '[{"dishId":"dish001","name":"麻婆豆腐","price":28,"quantity":1},{"dishId":"dish002","name":"宫保鸡丁","price":38,"quantity":2}]',
 104.00),
('push002', 'user002', '测试用户2',
 '[{"dishId":"dish003","name":"红烧肉","price":45,"quantity":1},{"dishId":"dish004","name":"糖醋排骨","price":42,"quantity":1}]',
 87.00)
ON DUPLICATE KEY UPDATE pusher_name=VALUES(pusher_name);
