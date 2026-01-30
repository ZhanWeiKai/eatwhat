package com.what2eat.controller;

import com.what2eat.dto.PhotoDTO;
import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.PhotoService;
import com.what2eat.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 相片Controller
 */
@RestController
@RequestMapping("/photos")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 上传照片
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PhotoDTO>> uploadPhoto(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {

        try {
            // 参数验证
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "请求参数不能为空"));
            }

            // 提取并验证参数
            Object userIdObj = request.get("userId");
            Object imageUrlObj = request.get("imageUrl");
            Object fileSizeObj = request.get("fileSize");

            if (userIdObj == null || imageUrlObj == null || fileSizeObj == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "缺少必要参数：userId、imageUrl、fileSize"));
            }

            String userId = userIdObj.toString();
            String imageUrl = imageUrlObj.toString();
            String description = request.get("description") != null ? request.get("description").toString() : "";

            // 转换fileSize，捕获可能的NumberFormatException
            Long fileSize;
            try {
                fileSize = Long.valueOf(fileSizeObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "文件大小格式错误"));
            }

            // 验证文件大小
            if (fileSize <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "文件大小必须大于0"));
            }

            PhotoDTO photo = photoService.uploadPhoto(userId, imageUrl, description, fileSize);

            return ResponseEntity.ok(ApiResponse.success("上传成功", photo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的所有照片
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PhotoDTO>>> getUserPhotos(@PathVariable String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "用户ID不能为空"));
            }

            List<PhotoDTO> photos = photoService.getUserPhotos(userId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", photos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户的所有照片
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PhotoDTO>>> getCurrentUserPhotos(
            @RequestHeader("Authorization") String token) {

        try {
            // 从JWT token中解析userId
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(401, "未授权"));
            }

            // 移除 "Bearer " 前缀
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            String userId = jwtUtil.getUserIdFromToken(jwtToken);

            List<PhotoDTO> photos = photoService.getUserPhotos(userId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", photos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 删除照片
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @RequestHeader("Authorization") String token,
            @PathVariable String photoId) {

        try {
            if (photoId == null || photoId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "照片ID不能为空"));
            }

            // 从JWT token中解析userId
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(401, "未授权"));
            }

            // 移除 "Bearer " 前缀
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            String userId = jwtUtil.getUserIdFromToken(jwtToken);

            photoService.deletePhoto(userId, photoId);

            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 获取单张照片详情
     */
    @GetMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoDTO>> getPhoto(@PathVariable String photoId) {
        try {
            if (photoId == null || photoId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "照片ID不能为空"));
            }

            PhotoDTO photo = photoService.getPhotoById(photoId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", photo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "服务器错误: " + e.getMessage()));
        }
    }
}
