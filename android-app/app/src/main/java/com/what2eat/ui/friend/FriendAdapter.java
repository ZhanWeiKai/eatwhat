package com.what2eat.ui.friend;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.what2eat.R;
import com.what2eat.data.model.FriendDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友列表适配器
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<FriendDTO> friendList = new ArrayList<>();

    public FriendAdapter(List<FriendDTO> friendList) {
        this.friendList = friendList != null ? friendList : new ArrayList<>();
    }

    public void setFriendList(List<FriendDTO> friendList) {
        this.friendList = friendList != null ? friendList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendDTO friend = friendList.get(position);

        // 设置昵称
        holder.tvNickname.setText(friend.getNickname() != null ? friend.getNickname() : "未知用户");

        // 显示在线状态
        if (friend.getOnline() != null && friend.getOnline()) {
            holder.statusIndicator.setImageResource(R.drawable.online_dot);
            holder.tvStatus.setText("在线");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色
        } else {
            holder.statusIndicator.setImageResource(R.drawable.offline_dot);
            holder.tvStatus.setText("离线");
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E")); // 灰色
        }

        // 加载头像
        String avatarUrl = friend.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.default_avatar);
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    /**
     * 更新好友在线状态
     */
    public void updateFriendStatus(String userId, boolean online) {
        for (int i = 0; i < friendList.size(); i++) {
            if (userId.equals(friendList.get(i).getUserId())) {
                friendList.get(i).setOnline(online);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        ImageView statusIndicator;
        TextView tvNickname;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            tvNickname = itemView.findViewById(R.id.tvNickname);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
