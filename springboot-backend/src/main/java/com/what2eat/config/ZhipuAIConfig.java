package com.what2eat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱AI配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zhipuai")
public class ZhipuAIConfig {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API URL
     */
    private String apiUrl;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 基础Prompt
     */
    private String basePrompt;
}
