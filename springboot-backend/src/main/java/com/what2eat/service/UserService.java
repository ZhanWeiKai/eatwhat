package com.what2eat.service;

import com.what2eat.dto.request.LoginRequest;
import com.what2eat.dto.request.RegisterRequest;
import com.what2eat.entity.User;
import com.what2eat.repository.UserRepository;
import com.what2eat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());

        return userRepository.save(user);
    }

    /**
     * 用户登录
     */
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成JWT Token
        return jwtUtil.generateToken(user.getUserId());
    }

    /**
     * 根据用户ID获取用户
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 验证Token并获取用户
     */
    public User validateTokenAndGetUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Token无效");
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        return getUserById(userId);
    }
}
