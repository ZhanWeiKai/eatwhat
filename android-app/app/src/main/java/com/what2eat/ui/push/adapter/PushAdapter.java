package com.what2eat.ui.push.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.what2eat.R;
import com.what2eat.data.model.Push;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 推送列表适配器
 */
public class PushAdapter extends RecyclerView.Adapter<PushAdapter.PushViewHolder> {

    private Context context;
    private List<Push> pushes;
    private String currentUserId;
    private OnPushDeleteListener listener;

    public interface OnPushDeleteListener {
        void onDeleteClick(Push push);
    }

    public PushAdapter(Context context, String currentUserId, OnPushDeleteListener listener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.pushes = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PushViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_push_card, parent, false);
        return new PushViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PushViewHolder holder, int position) {
        Push push = pushes.get(position);

        // 绑定数据
        holder.tvPusherName.setText(push.getPusherName() + " 推送");
        holder.tvTotalAmount.setText("¥" + push.getTotalAmount());

        // 设置时间（简化版，实际应该格式化）
        holder.tvPushTime.setText("刚刚");

        // 判断是否自己的推送
        boolean isOwnPush = currentUserId.equals(push.getPusherId());
        if (isOwnPush) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(push);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        // 动态添加菜品项
        holder.dishListContainer.removeAllViews();
        List<Push.DishItem> dishes = push.getDishes();
        if (dishes != null) {
            for (Push.DishItem dishItem : dishes) {
                View dishView = LayoutInflater.from(context).inflate(R.layout.item_push_dish, holder.dishListContainer, false);

                TextView tvDishName = dishView.findViewById(R.id.tvPushDishName);
                TextView tvDishPrice = dishView.findViewById(R.id.tvPushDishPrice);
                TextView tvDishQuantity = dishView.findViewById(R.id.tvPushDishQuantity);
                ImageView ivDishImage = dishView.findViewById(R.id.ivPushDishImage);

                tvDishName.setText(dishItem.getName() != null ? dishItem.getName() : "未知菜品");
                tvDishPrice.setText("¥" + dishItem.getPrice());
                tvDishQuantity.setText("x" + dishItem.getQuantity());

                // 可选：加载图片
                if (dishItem.getImageUrl() != null && !dishItem.getImageUrl().isEmpty()) {
                    Glide.with(context)
                            .load(dishItem.getImageUrl())
                            .apply(new RequestOptions()
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_gallery)
                                    .centerCrop())
                            .into(ivDishImage);
                }

                holder.dishListContainer.addView(dishView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return pushes.size();
    }

    /**
     * 设置推送列表
     */
    public void setPushes(List<Push> pushes) {
        this.pushes = pushes != null ? pushes : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * 删除推送
     */
    public void removePush(Push push) {
        int position = pushes.indexOf(push);
        if (position >= 0) {
            pushes.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class PushViewHolder extends RecyclerView.ViewHolder {
        TextView tvPusherName;
        TextView tvPushTime;
        TextView tvTotalAmount;
        Button btnDelete;
        LinearLayout dishListContainer;

        public PushViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPusherName = itemView.findViewById(R.id.tvPusherName);
            tvPushTime = itemView.findViewById(R.id.tvPushTime);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            dishListContainer = itemView.findViewById(R.id.dishListContainer);
        }
    }
}
