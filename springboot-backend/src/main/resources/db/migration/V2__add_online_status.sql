-- 添加用户在线状态字段
ALTER TABLE `user` ADD COLUMN `online_status` INT DEFAULT 0 COMMENT '在线状态：0=离线，1=在线';

-- 更新现有用户状态为离线
UPDATE `user` SET `online_status` = 0 WHERE `online_status` IS NULL;
