package com.what2eat.repository;

import com.what2eat.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜品Repository
 */
@Repository
public interface DishRepository extends JpaRepository<Dish, String> {

    /**
     * 根据分类查找菜品
     */
    List<Dish> findByCategory(String category);

    /**
     * 根据分类查找菜品，按创建时间倒序
     */
    List<Dish> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * 查找所有菜品，按创建时间倒序
     */
    List<Dish> findAllByOrderByCreatedAtDesc();

    /**
     * 查找所有菜品，先按分类升序，再按创建时间倒序
     */
    List<Dish> findAllByOrderByCategoryAscCreatedAtDesc();

    /**
     * 检查某个分类下是否有菜品
     */
    boolean existsByCategory(String category);
}
