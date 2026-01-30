-- 创建菜品分类表
CREATE TABLE IF NOT EXISTS `dish_category` (
    `category_id` VARCHAR(64) PRIMARY KEY COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 为name字段添加唯一索引
CREATE UNIQUE INDEX `idx_category_name` ON `dish_category`(`name`);

-- 插入初始分类数据
INSERT INTO `dish_category` (`category_id`, `name`, `sort_order`) VALUES
('cat001', '热菜', 1),
('cat002', '凉菜', 2),
('cat003', '主食', 3),
('cat004', '面食', 4),
('cat005', '汤品', 5),
('cat006', '甜点', 6),
('cat007', '饮品', 7);
