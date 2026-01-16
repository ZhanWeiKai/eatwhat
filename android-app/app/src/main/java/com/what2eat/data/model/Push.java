package com.what2eat.data.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推送记录数据模型
 */
public class Push {
    @SerializedName("pushId")
    private String pushId;

    @SerializedName("pusherId")
    private String pusherId;

    @SerializedName("pusherName")
    private String pusherName;

    @SerializedName("pusherAvatar")
    private String pusherAvatar;

    @SerializedName("dishes")
    private List<DishItem> dishes;

    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    @SerializedName("createdAt")
    private String createdAt;

    /**
     * 菜品项内部类
     */
    public static class DishItem {
        @SerializedName("dishId")
        private String dishId;

        @SerializedName("name")
        private String name;

        @SerializedName("price")
        private BigDecimal price;

        @SerializedName("quantity")
        private Integer quantity;

        @SerializedName("imageUrl")
        private String imageUrl;

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

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    // Getters and Setters
    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getPusherId() {
        return pusherId;
    }

    public void setPusherId(String pusherId) {
        this.pusherId = pusherId;
    }

    public String getPusherName() {
        return pusherName;
    }

    public void setPusherName(String pusherName) {
        this.pusherName = pusherName;
    }

    public String getPusherAvatar() {
        return pusherAvatar;
    }

    public void setPusherAvatar(String pusherAvatar) {
        this.pusherAvatar = pusherAvatar;
    }

    public List<DishItem> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishItem> dishes) {
        this.dishes = dishes;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
