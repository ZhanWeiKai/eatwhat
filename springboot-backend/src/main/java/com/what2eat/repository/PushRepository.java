package com.what2eat.repository;

import com.what2eat.entity.Push;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
