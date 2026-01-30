package com.what2eat.controller;

import com.what2eat.dto.BaiduTokenResponse;
import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.BaiduDriveService;
import com.what2eat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 百度网盘OAuth控制器
 */
@RestController
@RequestMapping("/baidu")
@RequiredArgsConstructor
public class BaiduDriveController {

    private final BaiduDriveService baiduDriveService;
    private final UserService userService;

    /**
     * 获取授权URL
     *
     * @param userId 用户ID
     * @return 授权URL
     */
    @GetMapping("/oauth/url")
    public ResponseEntity<ApiResponse<String>> getAuthorizationUrl(@RequestParam String userId) {
        try {
            String url = baiduDriveService.getAuthorizationUrl(userId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", url));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }

    /**
     * OAuth回调处理
     *
     * @param code  授权码
     * @param state 状态参数（用户ID）
     * @return HTML响应页面
     */
    @GetMapping("/oauth/callback")
    public String oauthCallback(
            @RequestParam String code,
            @RequestParam String state) {

        try {
            // 获取访问令牌
            BaiduTokenResponse token = baiduDriveService.getAccessToken(code);

            // 保存token到用户表
            userService.saveBaiduToken(state, token.getAccessToken(), token.getRefreshToken());

            // 返回成功页面
            return "<html><body>" +
                    "<h1>授权成功！</h1>" +
                    "<p>百度网盘已授权，请返回APP继续操作。</p>" +
                    "<script>setTimeout(function() { window.close(); }, 2000);</script>" +
                    "</body></html>";
        } catch (Exception e) {
            return "<html><body>" +
                    "<h1>授权失败</h1>" +
                    "<p>错误信息: " + e.getMessage() + "</p>" +
                    "</body></html>";
        }
    }
}
