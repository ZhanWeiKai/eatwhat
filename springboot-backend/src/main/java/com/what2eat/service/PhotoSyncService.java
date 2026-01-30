package com.what2eat.service;

import com.what2eat.entity.Photo;
import com.what2eat.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 照片同步服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoSyncService {

    private final BaiduDriveService baiduDriveService;
    private final PhotoRepository photoRepository;
    private final UserService userService;

    /**
     * 同步单张照片到百度网盘
     *
     * @param userId  用户ID
     * @param photoId 照片ID
     */
    @Transactional
    public void syncPhoto(String userId, String photoId) {
        log.info("开始同步照片: userId={}, photoId={}", userId, photoId);

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("照片不存在"));

        if (!photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作他人照片");
        }

        // 获取用户的百度网盘token
        String accessToken = userService.getBaiduAccessToken(userId);
        if (accessToken == null) {
            throw new IllegalArgumentException("请先授权百度网盘");
        }

        try {
            // 上传到百度网盘
            String fileName = extractFileName(photo.getImageUrl());
            String baiduFileId = baiduDriveService.uploadFile(accessToken, photo.getImageUrl(), fileName);

            // 更新照片记录
            photo.setBaiduFileId(baiduFileId);
            photo.setIsSynced(true);
            photoRepository.save(photo);

            log.info("照片同步成功: photoId={}, baiduFileId={}", photoId, baiduFileId);
        } catch (Exception e) {
            log.error("同步照片失败: photoId={}", photoId, e);
            throw new RuntimeException("同步失败: " + e.getMessage());
        }
    }

    /**
     * 批量同步照片
     *
     * @param userId    用户ID
     * @param photoIds 照片ID列表
     */
    @Transactional
    public void syncPhotos(String userId, List<String> photoIds) {
        log.info("开始批量同步照片: userId={}, count={}", userId, photoIds.size());

        int successCount = 0;
        int failCount = 0;

        for (String photoId : photoIds) {
            try {
                syncPhoto(userId, photoId);
                successCount++;
            } catch (Exception e) {
                log.error("同步照片失败: photoId={}", photoId, e);
                failCount++;
            }
        }

        log.info("批量同步完成: 成功={}, 失败={}", successCount, failCount);
    }

    /**
     * 从URL中提取文件名
     *
     * @param imageUrl 图片URL
     * @return 文件名
     */
    private String extractFileName(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 1];
    }
}
