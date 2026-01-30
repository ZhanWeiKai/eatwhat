package com.what2eat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(length = 64)
    private String userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 255)
    private String avatar;

    @Column(name = "online_status", columnDefinition = "INT DEFAULT 0")
    private Integer onlineStatus = 0;  // 0=离线, 1=在线

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 百度网盘访问令牌
     */
    @Column(name = "baidu_access_token", length = 500)
    private String baiduAccessToken;

    /**
     * 百度网盘刷新令牌
     */
    @Column(name = "baidu_refresh_token", length = 500)
    private String baiduRefreshToken;

    /**
     * 默认头像（使用配置的域名）
     */
    @Transient
    private String getDefaultAvatar() {
        // 从系统属性或默认值获取base URL
        String baseUrl = System.getProperty("file.base-url", "http://api.jamesweb.org:8883/api");
        return baseUrl + "/static/default-avatar.png";
    }

    /**
     * 判断用户是否在线
     */
    @Transient
    public boolean isOnline() {
        return onlineStatus != null && onlineStatus == 1;
    }

    public String getAvatar() {
        if (avatar != null && !avatar.isEmpty()) {
            // 如果头像URL是localhost开头，替换为域名
            if (avatar.contains("localhost:8883")) {
                return avatar.replace("http://localhost:8883/api", "http://api.jamesweb.org:8883/api");
            }
            if (avatar.contains("10.88.1.127:8883")) {
                return avatar.replace("http://10.88.1.127:8883/api", "http://api.jamesweb.org:8883/api");
            }
            return avatar;
        }
        return getDefaultAvatar();
    }
}
