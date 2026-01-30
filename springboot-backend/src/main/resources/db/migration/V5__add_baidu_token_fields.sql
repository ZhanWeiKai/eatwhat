-- 添加百度网盘Token字段到user表
ALTER TABLE user ADD COLUMN baidu_access_token VARCHAR(500) COMMENT '百度网盘访问令牌';
ALTER TABLE user ADD COLUMN baidu_refresh_token VARCHAR(500) COMMENT '百度网盘刷新令牌';
