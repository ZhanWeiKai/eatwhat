package com.what2eat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Entity
@Table(name = "dish")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {

    @Id
    @Column(length = 64)
    private String dishId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(length = 255)
    private String imageUrl;

    @Column(name = "uploader_id", length = 64)
    private String uploaderId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 默认图片（使用配置的域名）
     */
    @Transient
    private String getDefaultImage() {
        String baseUrl = System.getProperty("file.base-url", "http://api.jamesweb.org:8883/api");
        return baseUrl + "/static/default-dish.png";
    }

    public String getImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 如果图片URL是localhost或局域网IP开头，替换为域名
            if (imageUrl.contains("localhost:8883")) {
                return imageUrl.replace("http://localhost:8883/api", "http://api.jamesweb.org:8883/api");
            }
            if (imageUrl.contains("10.88.1.127:8883")) {
                return imageUrl.replace("http://10.88.1.127:8883/api", "http://api.jamesweb.org:8883/api");
            }
            return imageUrl;
        }
        return getDefaultImage();
    }
}
