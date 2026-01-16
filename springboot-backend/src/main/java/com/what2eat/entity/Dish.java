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
     * 默认图片
     */
    @Transient
    private static final String DEFAULT_IMAGE = "http://localhost:8883/api/static/default-dish.png";

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : DEFAULT_IMAGE;
    }
}
