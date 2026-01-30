package com.what2eat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 百度网盘配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.drive")
public class BaiduDriveConfig {
    /**
     * 百度网盘应用Key
     */
    private String appKey;

    /**
     * 百度网盘Secret Key
     */
    private String secretKey;

    /**
     * OAuth回调地址
     */
    private String redirectUri;

    /**
     * 上传API地址
     */
    private String uploadApi;

    /**
     * 授权URL
     */
    private String authorizeUrl;

    /**
     * Token获取URL
     */
    private String tokenUrl;
}
