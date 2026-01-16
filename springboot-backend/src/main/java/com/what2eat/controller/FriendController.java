package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.entity.Friendship;
import com.what2eat.service.FriendService;
import com.what2eat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友控制器
 */
@Tag(name = "好友管理", description = "添加好友、删除好友、获取好友列表等接口")
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    /**
     * 获取好友列表
     */
    @Operation(summary = "获取好友列表")
    @GetMapping
    public ApiResponse<List<Friendship>> getFriends(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            return ApiResponse.success(friendService.getFriends(userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 添加好友
     */
    @Operation(summary = "添加好友")
    @PostMapping("/add")
    public ApiResponse<Friendship> addFriend(
            @RequestParam String friendId,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            return ApiResponse.success("添加成功", friendService.addFriend(userId, friendId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除好友
     */
    @Operation(summary = "删除好友")
    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> deleteFriend(
            @PathVariable String friendId,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            friendService.deleteFriend(userId, friendId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
