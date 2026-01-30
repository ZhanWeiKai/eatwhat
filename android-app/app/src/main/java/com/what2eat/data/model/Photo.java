package com.what2eat.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 相片数据模型
 */
public class Photo implements Serializable {

    @SerializedName("photoId")
    private String photoId;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("fileSize")
    private Long fileSize;

    @SerializedName("isSynced")
    private Boolean isSynced;

    @SerializedName("createdAt")
    private String createdAt;

    public Photo() {
    }

    public Photo(String photoId, String imageUrl, String description, Long fileSize, Boolean isSynced, String createdAt) {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
