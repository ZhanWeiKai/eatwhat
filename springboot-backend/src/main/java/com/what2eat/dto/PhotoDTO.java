package com.what2eat.dto;

import java.time.LocalDateTime;

/**
 * 相片DTO
 */
public class PhotoDTO {

    private String photoId;
    private String imageUrl;
    private String description;
    private Long fileSize;
    private Boolean isSynced;
    private LocalDateTime createdAt;

    public PhotoDTO() {
    }

    public PhotoDTO(String photoId, String imageUrl, String description, Long fileSize, Boolean isSynced, LocalDateTime createdAt) {
        this.photoId = photoId;
        this.imageUrl = imageUrl;
        this.description = description;
        this.fileSize = fileSize;
        this.isSynced = isSynced;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Boolean getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(Boolean isSynced) {
        this.isSynced = isSynced;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
