package com.what2eat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 菜品分类实体类
 */
@Entity
@Table(name = "dish_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishCategory {

    @Id
    @Column(length = 64)
    private String categoryId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
