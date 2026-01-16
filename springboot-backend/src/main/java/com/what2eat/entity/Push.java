package com.what2eat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 推送记录实体类
 */
@Entity
@Table(name = "push")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Push {

    @Id
    @Column(length = 64)
    private String pushId;

    @Column(name = "pusher_id", nullable = false, length = 64)
    private String pusherId;

    @Column(name = "pusher_name", nullable = false, length = 50)
    private String pusherName;

    @Column(name = "pusher_avatar", length = 255)
    private String pusherAvatar;

    /**
     * 菜品列表JSON
     * 格式: [{"dishId":"xxx","name":"xxx","price":xx,"quantity":x},...]
     */
    @Column(nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DishItem> dishes;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 菜品项内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DishItem {
        private String dishId;
        private String name;
        private BigDecimal price;
        private Integer quantity;
        private String imageUrl;
    }

    /**
     * 默认推送者头像
     */
    @Transient
    private static final String DEFAULT_AVATAR = "http://localhost:8883/api/static/default-avatar.png";

    public String getPusherAvatar() {
        return pusherAvatar != null ? pusherAvatar : DEFAULT_AVATAR;
    }
}
