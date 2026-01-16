package com.what2eat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
@Entity
@Table(name = "friendship",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "friend_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "friend_id", nullable = false, length = 64)
    private String friendId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 检查是否是好友关系（双向）
     */
    public boolean isFriend(String userId1, String userId2) {
        return (this.userId.equals(userId1) && this.friendId.equals(userId2)) ||
               (this.userId.equals(userId2) && this.friendId.equals(userId1));
    }
}
