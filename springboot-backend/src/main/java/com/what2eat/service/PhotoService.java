package com.what2eat.service;

import com.what2eat.dto.PhotoDTO;
import com.what2eat.entity.Photo;
import com.what2eat.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 相片Service
 */
@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Value("${app.base-url:http://localhost:8883/api}")
    private String baseUrl;

    /**
     * 上传照片
     */
    @Transactional
    public PhotoDTO uploadPhoto(String userId, String imageUrl, String description, Long fileSize) {
        // 参数验证
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("图片URL不能为空");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("文件大小必须大于0");
        }

        Photo photo = new Photo();
        photo.setUserId(userId);
        photo.setImageUrl(imageUrl);
        photo.setDescription(description != null ? description : "");
        photo.setFileSize(fileSize);
        photo.setIsSynced(false); // 暂未同步到百度网盘

        Photo savedPhoto = photoRepository.save(photo);

        // 转换为DTO返回
        return convertToDTO(savedPhoto);
    }

    /**
     * 获取用户的所有照片
     */
    public List<PhotoDTO> getUserPhotos(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        List<Photo> photos = photoRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return photos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 删除照片
     */
    @Transactional
    public void deletePhoto(String userId, String photoId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (photoId == null || photoId.trim().isEmpty()) {
            throw new IllegalArgumentException("照片ID不能为空");
        }

        // 验证照片是否属于该用户
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("照片不存在"));

        if (!photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除他人的照片");
        }

        photoRepository.delete(photo);
    }

    /**
     * 获取单张照片详情
     */
    public PhotoDTO getPhotoById(String photoId) {
        if (photoId == null || photoId.trim().isEmpty()) {
            throw new IllegalArgumentException("照片ID不能为空");
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("照片不存在"));
        return convertToDTO(photo);
    }

    /**
     * 转换为DTO（处理图片URL）
     */
    private PhotoDTO convertToDTO(Photo photo) {
        if (photo == null) {
            return null;
        }

        String imageUrl = photo.getImageUrl();

        // 如果URL是相对路径，转换为绝对路径
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            imageUrl = baseUrl + "/uploads/" + imageUrl;
        }

        return new PhotoDTO(
                photo.getPhotoId(),
                imageUrl != null ? imageUrl : "",
                photo.getDescription(),
                photo.getFileSize(),
                photo.getIsSynced(),
                photo.getCreatedAt()
        );
    }
}
