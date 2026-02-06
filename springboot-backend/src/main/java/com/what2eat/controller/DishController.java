package com.what2eat.controller;

import com.what2eat.dto.AIDishRequest;
import com.what2eat.dto.response.ApiResponse;
import com.what2eat.entity.Dish;
import com.what2eat.service.DishService;
import com.what2eat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品控制器
 */
@Tag(name = "菜品管理", description = "菜品查询、上传、删除等接口")
@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;
    private final UserService userService;

    /**
     * 获取所有菜品
     */
    @Operation(summary = "获取所有菜品")
    @GetMapping
    public ApiResponse<List<Dish>> getAllDishes() {
        try {
            return ApiResponse.success(dishService.getAllDishes());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据分类获取菜品
     */
    @Operation(summary = "根据分类获取菜品")
    @GetMapping("/category/{category}")
    public ApiResponse<List<Dish>> getDishesByCategory(@PathVariable String category) {
        try {
            return ApiResponse.success(dishService.getDishesByCategory(category));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取菜品
     */
    @Operation(summary = "根据ID获取菜品")
    @GetMapping("/{id}")
    public ApiResponse<Dish> getDishById(@PathVariable String id) {
        try {
            return ApiResponse.success(dishService.getDishById(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 上传菜品
     */
    @Operation(summary = "上传菜品")
    @PostMapping
    public ApiResponse<Dish> createDish(
            @RequestBody Dish dish,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            return ApiResponse.success("上传成功", dishService.createDish(dish, userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除菜品
     */
    @Operation(summary = "删除菜品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDish(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {
        try {
            dishService.deleteDish(id);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有分类
     */
    @Operation(summary = "获取所有分类")
    @GetMapping("/categories/all")
    public ApiResponse<List<String>> getAllCategories() {
        try {
            return ApiResponse.success(dishService.getAllCategories());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 从AI推荐添加菜品
     */
    @Operation(summary = "从AI推荐添加菜品")
    @PostMapping("/add-from-ai")
    public ApiResponse<Dish> addDishFromAI(
            @RequestBody AIDishRequest request,
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String userId = userService.validateTokenAndGetUser(token).getUserId();

            // 创建菜品对象
            Dish dish = new Dish();
            dish.setName(request.getDishName());
            dish.setCategory(request.getCategory());
            dish.setCookingInstructions(request.getCookingMethod());
            dish.setDescription(request.getDishName()); // 使用菜品名作为描述
            dish.setPrice(BigDecimal.ZERO); // AI推荐的菜品默认价格为0

            return ApiResponse.success("添加成功", dishService.createDish(dish, userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
