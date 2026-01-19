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
import com.what2eat.data.model.Dish;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品适配器
 */
public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {

    private Context context;
    private List<Dish> dishes;
    private OnDishClickListener listener;

    public interface OnDishClickListener {
        void onAddClick(Dish dish);
    }

    public DishAdapter(Context context, OnDishClickListener listener) {
        this.context = context;
        this.dishes = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishes.get(position);

        // 绑定数据
        holder.tvDishName.setText(dish.getName());
        holder.tvDishDesc.setText(dish.getDescription());
        holder.tvDishPrice.setText("¥" + dish.getPrice());

        // 加载图片
        String imageUrl = dish.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .centerCrop())
                    .into(holder.ivDishImage);
        } else {
            holder.ivDishImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 添加按钮点击事件
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddClick(dish);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    /**
     * 设置菜品列表
     */
    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes != null ? dishes : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * 添加菜品
     */
    public void addDish(Dish dish) {
        this.dishes.add(dish);
        notifyItemInserted(dishes.size() - 1);
    }

    static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDishImage;
        TextView tvDishName;
        TextView tvDishDesc;
        TextView tvDishPrice;
        Button btnAdd;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.ivDishImage);
            tvDishName = itemView.findViewById(R.id.tvDishName);
            tvDishDesc = itemView.findViewById(R.id.tvDishDesc);
            tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
