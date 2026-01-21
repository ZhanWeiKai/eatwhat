package com.what2eat.repository;

import com.what2eat.entity.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 菜品分类Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<DishCategory, String> {

    /**
     * 根据名称查找分类
     */
    Optional<DishCategory> findByName(String name);

    /**
     * 查找所有分类，按排序字段升序
     */
    List<DishCategory> findAllByOrderBySortOrderAscCreatedAtAsc();

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);
}
