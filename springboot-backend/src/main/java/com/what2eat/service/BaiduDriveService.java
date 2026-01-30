package com.what2eat.service;

import com.what2eat.config.BaiduDriveConfig;
import com.what2eat.dto.BaiduTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 百度网盘服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduDriveService {

    private final BaiduDriveConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取授权URL
     *
     * @param state 状态参数，用于防止CSRF攻击
     * @return 授权URL
     */
    public String getAuthorizationUrl(String state) {
        return String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                config.getAuthorizeUrl(),
                config.getAppKey(),
                config.getRedirectUri(),
                "netdisk",
                state);
    }

    /**
     * 通过授权码获取访问令牌
     *
     * @param code 授权码
     * @return Token响应
     */
    public BaiduTokenResponse getAccessToken(String code) {
        String url = String.format("%s?grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s",
                config.getTokenUrl(),
                code,
                config.getAppKey(),
                config.getSecretKey(),
                config.getRedirectUri());

        log.info("获取百度网盘访问令牌: code={}", code);
        BaiduTokenResponse response = restTemplate.getForObject(url, BaiduTokenResponse.class);
        log.info("获取访问令牌成功: accessToken={}", response != null ? response.getAccessToken().substring(0, 10) + "..." : "null");

        return response;
    }

    /**
     * 上传文件到百度网盘
     *
     * @param accessToken 访问令牌
     * @param fileUrl     文件URL
     * @param fileName    文件名
     * @return 百度网盘文件ID
     */
    public String uploadFile(String accessToken, String fileUrl, String fileName) {
        try {
            log.info("开始上传文件到百度网盘: fileName={}", fileName);

            // 1. 预上传获取uploadid
            String preUploadUrl = String.format("%s?method=precreate&access_token=%s",
                    config.getUploadApi(), accessToken);

            Map<String, Object> preUploadData = new HashMap<>();
            preUploadData.put("path", "/apps/what2eat/" + fileName);
            preUploadData.put("size", 1024); // TODO: 获取实际文件大小
            preUploadData.put("block_list", new Object[]{"[]"});

            // TODO: 实现分片上传逻辑
            // 这里先返回占位符
            log.info("预上传URL: {}", preUploadUrl);
            log.warn("文件上传功能尚未完全实现，返回占位符");
            return "placeholder_file_id_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("上传文件到百度网盘失败", e);
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
    }
}
