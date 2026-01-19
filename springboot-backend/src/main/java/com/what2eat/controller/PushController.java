package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.entity.Push;
import com.what2eat.entity.User;
import com.what2eat.service.PushService;
import com.what2eat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推送控制器
 */
@Tag(name = "推送管理", description = "菜单推送、查询、删除等接口")
@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {

    private final PushService pushService;
    private final UserService userService;

    /**
     * 获取所有推送记录（只显示好友的推送）
     */
    @Operation(summary = "获取所有推送记录")
    @GetMapping("/list")
    public ApiResponse<List<Push>> getAllPushes(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            // 只返回好友的推送
            return ApiResponse.success(pushService.getPushesForUser(userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取推送记录
     */
    @Operation(summary = "根据ID获取推送记录")
    @GetMapping("/{id}")
    public ApiResponse<Push> getPushById(@PathVariable String id) {
        try {
            return ApiResponse.success(pushService.getPushById(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建推送
     */
    @Operation(summary = "推送菜单")
    @PostMapping
    public ApiResponse<Push> createPush(
            @RequestBody Push push,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            // 设置推送人信息
            User user = userService.validateTokenAndGetUser(token);
            push.setPusherName(user.getNickname());
            push.setPusherAvatar(user.getAvatar());

            return ApiResponse.success("推送成功", pushService.createPush(push, userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除推送
     */
    @Operation(summary = "删除推送")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePush(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            pushService.deletePush(id, userId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
