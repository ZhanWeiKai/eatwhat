package com.what2eat.service;

import com.what2eat.entity.DishCategory;
import com.what2eat.repository.CategoryRepository;
import com.what2eat.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 菜品分类服务
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;

    /**
     * 获取所有分类
     */
    public List<DishCategory> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAscCreatedAtAsc();
    }

    /**
     * 根据ID获取分类
     */
    public DishCategory getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
    }

    /**
     * 创建分类
     */
    @Transactional
    public DishCategory createCategory(DishCategory category) {
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("分类名称已存在");
        }

        // 设置默认排序值
        if (category.getSortOrder() == null) {
            Integer maxSortOrder = categoryRepository.findAll().stream()
                    .map(DishCategory::getSortOrder)
                    .max(Integer::compareTo)
                    .orElse(0);
            category.setSortOrder(maxSortOrder + 1);
        }

        category.setCategoryId(UUID.randomUUID().toString());
        return categoryRepository.save(category);
    }

    /**
     * 更新分类
     */
    @Transactional
    public DishCategory updateCategory(String categoryId, DishCategory category) {
        DishCategory existingCategory = getCategoryById(categoryId);

        // 如果修改了名称，检查新名称是否已存在
        if (!existingCategory.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(category.getName())) {
                throw new RuntimeException("分类名称已存在");
            }
            existingCategory.setName(category.getName());
        }

        // 更新排序
        if (category.getSortOrder() != null) {
            existingCategory.setSortOrder(category.getSortOrder());
        }

        return categoryRepository.save(existingCategory);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(String categoryId) {
        DishCategory category = getCategoryById(categoryId);

        // 检查是否有菜品使用该分类
        if (dishRepository.existsByCategory(category.getName())) {
            throw new RuntimeException("该分类下还有菜品，无法删除");
        }

        categoryRepository.deleteById(categoryId);
    }
}
