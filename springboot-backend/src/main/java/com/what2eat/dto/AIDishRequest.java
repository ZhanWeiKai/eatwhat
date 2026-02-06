package com.what2eat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI推荐菜品请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIDishRequest {
    
    /**
     * 菜品名称
     */
    private String dishName;
    
    /**
     * 菜品分类
     */
    private String category;
    
    /**
     * 烹饪方法
     */
    private String cookingMethod;
}
