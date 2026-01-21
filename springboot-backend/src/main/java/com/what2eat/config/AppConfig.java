package com.what2eat.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用配置
 */
@Data
@Component
public class AppConfig {

    @Value("${file.base-url}")
    private String fileBaseUrl;
}
