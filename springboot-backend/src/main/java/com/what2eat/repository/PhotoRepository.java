package com.what2eat.repository;

import com.what2eat.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 相片Repository
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {

    /**
     * 根据用户ID查询所有照片（按创建时间倒序）
     */
    List<Photo> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 根据用户ID删除照片
     */
    void deleteByUserIdAndPhotoId(String userId, String photoId);
}
