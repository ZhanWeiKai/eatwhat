package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理", description = "文件上传接口")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传图片
     */
    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileService.uploadImage(file);

            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);

            return ApiResponse.success("上传成功", data);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
