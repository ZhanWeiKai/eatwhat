package com.what2eat.ui.menu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.what2eat.R;
import com.what2eat.utils.ShoppingCartManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车适配器
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<ShoppingCartManager.CartItem> cartItems;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onIncreaseClick(ShoppingCartManager.CartItem item);
        void onDecreaseClick(ShoppingCartManager.CartItem item);
        void onRemoveClick(ShoppingCartManager.CartItem item);
    }

    public CartAdapter(Context context, OnCartItemClickListener listener) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        ShoppingCartManager.CartItem item = cartItems.get(position);

        // 绑定数据
        holder.tvCartDishName.setText(item.getName());
        holder.tvCartDishPrice.setText("¥" + item.getPrice());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // 加载图片
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .centerCrop())
                    .into(holder.ivCartDishImage);
        } else {
            holder.ivCartDishImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 数量增加
        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncreaseClick(item);
            }
        });

        // 数量减少
        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecreaseClick(item);
            }
        });

        // 删除
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * 设置购物车列表
     */
    public void setCartItems(List<ShoppingCartManager.CartItem> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartDishImage;
        TextView tvCartDishName;
        TextView tvCartDishPrice;
        TextView tvQuantity;
        Button btnDecrease;
        Button btnIncrease;
        Button btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartDishImage = itemView.findViewById(R.id.ivCartDishImage);
            tvCartDishName = itemView.findViewById(R.id.tvCartDishName);
            tvCartDishPrice = itemView.findViewById(R.id.tvCartDishPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
