package com.what2eat.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    // 允许的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    /**
     * 上传图片
     */
    public String uploadImage(MultipartFile file) throws IOException {
        logger.info("开始上传文件: {}, contentType: {}, size: {}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        // 验证文件
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 验证文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            logger.error("不支持的文件格式: {}", extension);
            throw new RuntimeException("只能上传图片文件，支持的格式: jpg, jpeg, png, gif, bmp, webp");
        }

        // 文件扩展名验证通过，继续处理

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("创建上传目录: {}", uploadPath.toAbsolutePath());
        }

        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        logger.info("文件上传成功: {}", filePath.toAbsolutePath());

        // 返回访问URL
        return baseUrl + "/static/" + filename;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
