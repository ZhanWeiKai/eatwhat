package com.what2eat.ui.photo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.what2eat.R;
import com.what2eat.data.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册Adapter
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context context;
    private List<Photo> photoList;

    public PhotoAdapter(Context context, List<Photo> photoList) {
        this.context = context;
        this.photoList = photoList != null ? photoList : new java.util.ArrayList<>();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        // 防御性检查
        if (photoList == null || position >= photoList.size()) {
            return;
        }

        Photo photo = photoList.get(position);
        if (photo == null) {
            return;
        }

        String imageUrl = photo.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 使用Glide加载图片
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            // 如果没有URL，显示占位图
            holder.setImagePlaceholder();
        }

        // 添加点击事件查看大图，传递完整照片列表和位置
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            intent.putExtra(PhotoDetailActivity.EXTRA_IMAGE_URL, imageUrl);
            intent.putExtra(PhotoDetailActivity.EXTRA_PHOTO_ID, photo.getPhotoId());
            intent.putExtra(PhotoDetailActivity.EXTRA_PHOTO_POSITION, holder.getAdapterPosition());
            intent.putExtra(PhotoDetailActivity.EXTRA_PHOTO_LIST, new ArrayList<>(photoList));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return photoList != null ? photoList.size() : 0;
    }

    /**
     * 更新照片列表
     */
    public void updatePhotos(List<Photo> newPhotos) {
        this.photoList = newPhotos != null ? newPhotos : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPhoto);
        }

        public void setImagePlaceholder() {
            if (imageView != null) {
                imageView.setImageResource(R.drawable.placeholder);
            }
        }
    }
}
