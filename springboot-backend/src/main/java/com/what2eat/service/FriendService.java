package com.what2eat.service;

import com.what2eat.entity.Friendship;
import com.what2eat.entity.User;
import com.what2eat.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 好友服务
 */
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserService userService;

    /**
     * 获取用户的好友列表
     */
    public List<Friendship> getFriends(String userId) {
        return friendshipRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 添加好友（通过userId）
     */
    @Transactional
    public Friendship addFriend(String userId, String friendId) {
        // 验证用户是否存在
        userService.getUserById(friendId);

        // 检查是否已经是好友
        if (friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new RuntimeException("已经是好友了");
        }

        // 不能添加自己为好友
        if (userId.equals(friendId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 创建好友关系（单向）
        Friendship friendship = new Friendship();
        friendship.setId(UUID.randomUUID().toString());
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);

        return friendshipRepository.save(friendship);
    }

    /**
     * 删除好友
     */
    @Transactional
    public void deleteFriend(String userId, String friendId) {
        Friendship friendship = friendshipRepository
                .findByUserId(userId)
                .stream()
                .filter(f -> f.getFriendId().equals(friendId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("好友关系不存在"));

        friendshipRepository.delete(friendship);
    }
}
