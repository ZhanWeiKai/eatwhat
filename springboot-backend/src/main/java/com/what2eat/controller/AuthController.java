package com.what2eat.controller;

import com.what2eat.dto.request.LoginRequest;
import com.what2eat.dto.request.RegisterRequest;
import com.what2eat.dto.response.ApiResponse;
import com.what2eat.entity.User;
import com.what2eat.repository.UserRepository;
import com.what2eat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、登出等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);

            // 自动登录，返回token
            String token = userService.login(new LoginRequest(
                    request.getUsername(),
                    request.getPassword()
            ));

            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getUserId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());

            return ApiResponse.success("注册成功", data);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            User user = userService.validateTokenAndGetUser(token);

            // 设置用户为在线状态
            user.setOnlineStatus(1);
            userRepository.save(user);

            // 广播用户状态变化
            Map<String, Object> statusMessage = new HashMap<>();
            statusMessage.put("userId", user.getUserId());
            statusMessage.put("nickname", user.getNickname());
            statusMessage.put("status", 1);  // 1=在线
            messagingTemplate.convertAndSend("/topic/user-status", statusMessage);

            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getUserId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatar", user.getAvatar());

            return ApiResponse.success("登录成功", data);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            User user = userService.validateTokenAndGetUser(token);
            return ApiResponse.success(user);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 如果提供了token，则更新用户状态
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.replace("Bearer ", "");
                User user = userService.validateTokenAndGetUser(token);

                // 设置用户为离线状态
                user.setOnlineStatus(0);
                userRepository.save(user);

                // 广播用户状态变化
                Map<String, Object> statusMessage = new HashMap<>();
                statusMessage.put("userId", user.getUserId());
                statusMessage.put("nickname", user.getNickname());
                statusMessage.put("status", 0);  // 0=离线
                messagingTemplate.convertAndSend("/topic/user-status", statusMessage);
            }

            return ApiResponse.success("退出成功", null);
        } catch (RuntimeException e) {
            // 即使token无效，也返回成功（客户端删除token即可）
            return ApiResponse.success("退出成功", null);
        }
    }
}
