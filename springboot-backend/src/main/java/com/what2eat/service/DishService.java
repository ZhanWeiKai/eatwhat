package com.what2eat.service;

import com.what2eat.entity.Dish;
import com.what2eat.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 菜品服务
 */
@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;

    /**
     * 获取所有菜品（按分类排序，同一分类内按创建时间倒序）
     */
    public List<Dish> getAllDishes() {
        return dishRepository.findAllByOrderByCategoryAscCreatedAtDesc();
    }

    /**
     * 根据分类获取菜品
     */
    public List<Dish> getDishesByCategory(String category) {
        return dishRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    /**
     * 根据ID获取菜品
     */
    public Dish getDishById(String dishId) {
        return dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));
    }

    /**
     * 创建菜品
     */
    @Transactional
    public Dish createDish(Dish dish, String uploaderId) {
        dish.setDishId(UUID.randomUUID().toString());
        dish.setUploaderId(uploaderId);
        return dishRepository.save(dish);
    }

    /**
     * 删除菜品
     */
    @Transactional
    public void deleteDish(String dishId) {
        if (!dishRepository.existsById(dishId)) {
            throw new RuntimeException("菜品不存在");
        }
        dishRepository.deleteById(dishId);
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return dishRepository.findAll().stream()
                .map(Dish::getCategory)
                .distinct()
                .toList();
    }
}
