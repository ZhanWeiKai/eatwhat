package com.what2eat.service;

import com.what2eat.entity.Push;
import com.what2eat.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 推送服务
 */
@Service
@RequiredArgsConstructor
public class PushService {

    private final PushRepository pushRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 获取所有推送记录
     */
    public List<Push> getAllPushes() {
        return pushRepository.findAllByOrderByCreatedAtDesc();
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
     * 创建推送
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

        Push savedPush = pushRepository.save(push);

        // 通过WebSocket推送给所有客户端
        messagingTemplate.convertAndSend("/topic/push", savedPush);

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
