-- 创建相片表
CREATE TABLE IF NOT EXISTS photo (
    photo_id VARCHAR(36) PRIMARY KEY COMMENT '照片唯一标识',
    user_id VARCHAR(36) NOT NULL COMMENT '上传者ID',
    image_url VARCHAR(500) NOT NULL COMMENT '服务器存储的图片URL',
    baidu_file_id VARCHAR(100) COMMENT '百度网盘文件ID',
    description VARCHAR(500) COMMENT '照片描述',
    file_size BIGINT COMMENT '文件大小（字节）',
    is_synced BOOLEAN DEFAULT FALSE COMMENT '是否已同步到百度网盘',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相片表';
