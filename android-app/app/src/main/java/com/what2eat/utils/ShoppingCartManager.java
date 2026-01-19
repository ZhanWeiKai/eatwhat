package com.what2eat.utils;

import com.what2eat.data.model.Dish;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车管理器（单例模式）
 * 负责管理购物车中的菜品数据
 */
public class ShoppingCartManager {

    private static ShoppingCartManager instance;
    private List<CartItem> items;

    private ShoppingCartManager() {
        items = new ArrayList<>();
    }

    /**
     * 获取单例实例
     */
    public static synchronized ShoppingCartManager getInstance() {
        if (instance == null) {
            instance = new ShoppingCartManager();
        }
        return instance;
    }

    /**
     * 添加菜品到购物车
     * 如果菜品已存在，则增加数量
     */
    public void addItem(Dish dish) {
        for (CartItem item : items) {
            if (item.getDishId().equals(dish.getDishId())) {
                // 菜品已存在，增加数量
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        // 菜品不存在，添加新项
        items.add(new CartItem(dish));
    }

    /**
     * 移除菜品
     */
    public void removeItem(String dishId) {
        items.removeIf(item -> item.getDishId().equals(dishId));
    }

    /**
     * 更新菜品数量
     */
    public void updateQuantity(String dishId, int quantity) {
        for (CartItem item : items) {
            if (item.getDishId().equals(dishId)) {
                if (quantity <= 0) {
                    // 数量为0时移除
                    items.remove(item);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    /**
     * 清空购物车
     */
    public void clear() {
        items.clear();
    }

    /**
     * 获取所有购物车项
     */
    public List<CartItem> getItems() {
        return items;
    }

    /**
     * 获取购物车商品总数量
     */
    public int getTotalCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * 获取购物车总金额
     */
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    /**
     * 检查购物车是否为空
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 购物车项
     */
    public static class CartItem {
        private String dishId;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        private String category;
        private int quantity;

        /**
         * 从Dish对象构造CartItem
         */
        public CartItem(Dish dish) {
            this.dishId = dish.getDishId();
            this.name = dish.getName();
            this.description = dish.getDescription();
            this.price = dish.getPrice();
            this.imageUrl = dish.getImageUrl();
            this.category = dish.getCategory();
            this.quantity = 1;
        }

        // Getters and Setters

        public String getDishId() {
            return dishId;
        }

        public void setDishId(String dishId) {
            this.dishId = dishId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
