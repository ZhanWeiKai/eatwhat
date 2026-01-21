package com.what2eat.repository;

import com.what2eat.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 好友关系Repository
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    /**
     * 查找用户的所有好友
     */
    List<Friendship> findByUserId(String userId);

    /**
     * 查找用户的所有好友，按创建时间倒序
     */
    List<Friendship> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 查找两个用户之间是否已经是好友
     */
    boolean existsByUserIdAndFriendId(String userId, String friendId);

    /**
     * 查找所有关注该用户的人（反向好友关系）
     */
    List<Friendship> findByFriendId(String friendId);
}
