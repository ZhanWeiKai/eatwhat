package com.what2eat.repository;

import com.what2eat.entity.Push;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 推送记录Repository
 */
@Repository
public interface PushRepository extends JpaRepository<Push, String> {

    /**
     * 查找所有推送记录，按创建时间倒序
     */
    List<Push> findAllByOrderByCreatedAtDesc();

    /**
     * 根据推送人ID查找推送记录
     */
    List<Push> findByPusherIdOrderByCreatedAtDesc(String pusherId);

    /**
     * 根据推送人ID列表查找推送记录
     */
    @Query("SELECT p FROM Push p WHERE p.pusherId IN :pusherIds ORDER BY p.createdAt DESC")
    List<Push> findByPusherIdInOrderByCreatedAtDesc(List<String> pusherIds);
}
