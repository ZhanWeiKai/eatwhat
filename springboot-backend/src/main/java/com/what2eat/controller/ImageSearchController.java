package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.ImageSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片搜索API控制器
 */
@RestController
@RequestMapping("/image")
@Slf4j
public class ImageSearchController {

    @Autowired
    private ImageSearchService imageSearchService;

    /**
     * 搜索菜品图片
     *
     * @param request 请求体，包含用户查询
     * @return 图片URL
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, String>>> searchDishImage(
            @RequestBody Map<String, String> request) {

        try {
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "查询内容不能为空"));
            }

            log.info("收到图片搜索请求: {}", query);

            // 调用服务搜索图片
            String imageUrl = imageSearchService.searchDishImage(query);

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                log.warn("未找到合适的图片: {}", query);
                return ResponseEntity.ok(ApiResponse.error(404, "未找到合适的图片"));
            }

            log.info("成功找到图片: {}", imageUrl);
            return ResponseEntity.ok(ApiResponse.success("搜索成功",
                Map.of("imageUrl", imageUrl, "query", query)));

        } catch (Exception e) {
            log.error("图片搜索失败", e);
            return ResponseEntity.ok(ApiResponse.error(500, "图片搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("图片搜索服务运行正常", "OK"));
    }
}
