package com.what2eat.ui.menu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.what2eat.R;

import java.util.List;

/**
 * 分类适配器
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<String> categories;
    private int selectedPosition = 0; // 默认选中第一个
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category, int position);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);

        // 设置选中状态
        if (position == selectedPosition) {
            holder.tvCategoryName.setTextColor(context.getResources().getColor(R.color.primary));
            holder.selectedIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategoryName.setTextColor(context.getResources().getColor(R.color.text_primary));
            holder.selectedIndicator.setVisibility(View.GONE);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategoryClick(category, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * 设置选中的分类
     */
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        View selectedIndicator;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            selectedIndicator = itemView.findViewById(R.id.selectedIndicator);
        }
    }
}
