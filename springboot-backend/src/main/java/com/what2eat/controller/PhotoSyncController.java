package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.PhotoSyncService;
import com.what2eat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 照片同步控制器
 */
@RestController
@RequestMapping("/photos/sync")
@RequiredArgsConstructor
public class PhotoSyncController {

    private final PhotoSyncService photoSyncService;
    private final JwtUtil jwtUtil;

    /**
     * 同步单张照片
     *
     * @param token   JWT Token
     * @param photoId 照片ID
     * @return 同步结果
     */
    @PostMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> syncPhoto(
            @RequestHeader("Authorization") String token,
            @PathVariable String photoId) {

        try {
            String userId = getUserIdFromToken(token);
            photoSyncService.syncPhoto(userId, photoId);
            return ResponseEntity.ok(ApiResponse.success("同步成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "同步失败: " + e.getMessage()));
        }
    }

    /**
     * 批量同步照片
     *
     * @param token    JWT Token
     * @param request  包含photoIds的请求体
     * @return 同步结果
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncPhotos(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, List<String>> request) {

        try {
            String userId = getUserIdFromToken(token);
            List<String> photoIds = request.get("photoIds");

            if (photoIds == null || photoIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "请选择要同步的照片"));
            }

            photoSyncService.syncPhotos(userId, photoIds);

            return ResponseEntity.ok(ApiResponse.success("批量同步已启动", Map.of(
                    "total", photoIds.size(),
                    "status", "processing"
            )));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "同步失败: " + e.getMessage()));
        }
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    private String getUserIdFromToken(String token) {
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtUtil.getUserIdFromToken(jwtToken);
    }
}
