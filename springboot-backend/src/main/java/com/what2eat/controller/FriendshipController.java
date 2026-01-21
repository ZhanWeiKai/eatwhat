package com.what2eat.controller;

import com.what2eat.dto.FriendDTO;
import com.what2eat.dto.response.ApiResponse;
import com.what2eat.entity.Friendship;
import com.what2eat.entity.User;
import com.what2eat.repository.FriendshipRepository;
import com.what2eat.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 好友关系控制器
 */
@Tag(name = "好友管理", description = "好友列表、好友状态等接口")
@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * 获取用户的好友列表（包含在线状态）
     */
    @Operation(summary = "获取好友列表")
    @GetMapping("/list")
    public ApiResponse<List<FriendDTO>> getFriendList(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            String userId = null;

            // 尝试从token获取用户ID（可选）
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.replace("Bearer ", "");
                // 注意：这里简化处理，实际应该通过UserService验证token
                // 暂时通过token中的userId（如果token包含的话）
                // 这里假设前端会传递userId作为查询参数
            }

            // TODO: 从token中提取userId，或从请求参数获取
            // 为了测试，暂时返回空列表或使用固定用户
            // 实际应该从JWT token中解析userId

            return ApiResponse.success("获取成功", new ArrayList<>());

        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据userId获取好友列表
     */
    @Operation(summary = "根据用户ID获取好友列表")
    @GetMapping("/list/{userId}")
    public ApiResponse<List<FriendDTO>> getFriendListByUserId(@PathVariable String userId) {
        try {
            // 查询双向好友关系
            List<Friendship> iFollow = friendshipRepository.findByUserId(userId);
            List<Friendship> followMe = friendshipRepository.findByFriendId(userId);

            // 合并去重
            Set<String> friendIds = new HashSet<>();
            for (Friendship f : iFollow) {
                friendIds.add(f.getFriendId());
            }
            for (Friendship f : followMe) {
                friendIds.add(f.getUserId());
            }
            friendIds.remove(userId); // 移除自己

            // 获取好友详细信息
            List<FriendDTO> friends = new ArrayList<>();
            for (String friendId : friendIds) {
                userRepository.findById(friendId).ifPresent(user -> {
                    FriendDTO dto = new FriendDTO();
                    dto.setUserId(user.getUserId());
                    dto.setNickname(user.getNickname());
                    dto.setAvatar(user.getAvatar());
                    dto.setOnline(user.isOnline());
                    friends.add(dto);
                });
            }

            return ApiResponse.success("获取成功", friends);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
