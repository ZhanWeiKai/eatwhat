package com.what2eat.service;

import com.what2eat.entity.Push;
import com.what2eat.repository.FriendshipRepository;
import com.what2eat.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 推送服务
 */
@Service
@RequiredArgsConstructor
public class PushService {

    private final PushRepository pushRepository;
    private final FriendshipRepository friendshipRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 获取所有推送记录
     */
    public List<Push> getAllPushes() {
        return pushRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 获取用户好友的推送记录
     */
    public List<Push> getPushesForUser(String userId) {
        // 获取用户的所有好友关系
        List<com.what2eat.entity.Friendship> friendships = friendshipRepository.findByUserId(userId);

        // 收集所有好友的ID（包括自己，因为也能看到自己的推送）
        List<String> friendIds = friendships.stream()
                .map(f -> f.getFriendId())
                .collect(Collectors.toList());

        // 添加自己，这样也能看到自己推送的菜单
        friendIds.add(userId);

        // 查询这些好友的推送记录
        return pushRepository.findByPusherIdInOrderByCreatedAtDesc(friendIds);
    }

    /**
     * 根据推送人ID获取推送记录
     */
    public List<Push> getPushesByPusherId(String pusherId) {
        return pushRepository.findByPusherIdOrderByCreatedAtDesc(pusherId);
    }

    /**
     * 根据ID获取推送记录
     */
    public Push getPushById(String pushId) {
        return pushRepository.findById(pushId)
                .orElseThrow(() -> new RuntimeException("推送记录不存在"));
    }

    /**
     * 创建推送并发送给所有好友（双向好友关系）
     */
    @Transactional
    public Push createPush(Push push, String pusherId) {
        push.setPushId(UUID.randomUUID().toString());
        push.setPusherId(pusherId);

        // 计算总金额
        BigDecimal totalAmount = push.getDishes().stream()
                .map(dish -> dish.getPrice().multiply(BigDecimal.valueOf(dish.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        push.setTotalAmount(totalAmount);

        // 保存推送记录
        Push savedPush = pushRepository.save(push);

        // 查询双向好友关系：
        // 1. 我关注的人（我扫他们的二维码）
        List<com.what2eat.entity.Friendship> iFollow = friendshipRepository.findByUserId(pusherId);
        // 2. 关注我的人（他们扫我的二维码）
        List<com.what2eat.entity.Friendship> followMe = friendshipRepository.findByFriendId(pusherId);

        // 合并好友ID列表（去重）
        java.util.Set<String> friendIdSet = new java.util.HashSet<>();
        for (com.what2eat.entity.Friendship f : iFollow) {
            friendIdSet.add(f.getFriendId());
        }
        for (com.what2eat.entity.Friendship f : followMe) {
            friendIdSet.add(f.getUserId());
        }

        // 排除推送者自己（不要给自己发推送）
        friendIdSet.remove(pusherId);

        List<String> friendIds = new java.util.ArrayList<>(friendIdSet);

        // 遍历所有好友，通过WebSocket推送给每个在线好友
        int successCount = 0;
        for (String friendId : friendIds) {
            try {
                // 推送到该好友的用户专属频道
                messagingTemplate.convertAndSend(
                    "/topic/user/" + friendId,
                    savedPush
                );
                successCount++;
                System.out.println("推送消息已发送给用户: " + friendId);
            } catch (Exception e) {
                // 某个好友推送失败不影响其他好友
                System.err.println("推送消息给用户 " + friendId + " 失败: " + e.getMessage());
            }
        }

        System.out.println("推送消息已发送给 " + successCount + "/" + friendIds.size() + " 个好友");
        return savedPush;
    }

    /**
     * 删除推送
     */
    @Transactional
    public void deletePush(String pushId, String userId) {
        Push push = getPushById(pushId);

        // 验证权限：只有推送者本人可以删除
        if (!push.getPusherId().equals(userId)) {
            throw new RuntimeException("无权删除此推送");
        }

        pushRepository.deleteById(pushId);
    }
}
