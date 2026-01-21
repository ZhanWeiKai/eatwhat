package com.what2eat.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 菜品分类数据模型
 */
public class DishCategory {

    @SerializedName("categoryId")
    private String categoryId;

    @SerializedName("name")
    private String name;

    @SerializedName("sortOrder")
    private int sortOrder;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
