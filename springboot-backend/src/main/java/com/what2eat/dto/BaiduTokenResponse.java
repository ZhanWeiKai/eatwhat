package com.what2eat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 百度网盘Token响应DTO
 */
@Data
public class BaiduTokenResponse {

    /**
     * 访问令牌
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 过期时间（秒）
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * 会话密钥
     */
    @JsonProperty("session_key")
    private String sessionKey;

    /**
     * 会话密钥
     */
    @JsonProperty("session_secret")
    private String sessionSecret;
}
