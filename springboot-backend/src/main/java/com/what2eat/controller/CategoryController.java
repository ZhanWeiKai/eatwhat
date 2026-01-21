package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.entity.DishCategory;
import com.what2eat.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类控制器
 */
@Tag(name = "分类管理", description = "菜品分类的增删改查接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类
     */
    @Operation(summary = "获取所有分类")
    @GetMapping
    public ApiResponse<List<DishCategory>> getAllCategories() {
        try {
            return ApiResponse.success(categoryService.getAllCategories());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取分类
     */
    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public ApiResponse<DishCategory> getCategoryById(@PathVariable String id) {
        try {
            return ApiResponse.success(categoryService.getCategoryById(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建分类
     */
    @Operation(summary = "创建分类")
    @PostMapping
    public ApiResponse<DishCategory> createCategory(@RequestBody DishCategory category) {
        try {
            return ApiResponse.success("创建成功", categoryService.createCategory(category));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public ApiResponse<DishCategory> updateCategory(
            @PathVariable String id,
            @RequestBody DishCategory category) {
        try {
            return ApiResponse.success("更新成功", categoryService.updateCategory(id, category));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
        try {
            categoryService.deleteCategory(id);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
