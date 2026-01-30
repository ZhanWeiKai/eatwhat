package com.what2eat.ui.photo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.what2eat.R;
import com.what2eat.data.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片详情Activity - 显示放大的照片，支持左右滑动
 */
public class PhotoDetailActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_PHOTO_ID = "photo_id";
    public static final String EXTRA_PHOTO_POSITION = "photo_position";
    public static final String EXTRA_PHOTO_LIST = "photo_list";

    private ViewPager2 viewPager;
    private PhotoPagerAdapter adapter;
    private List<Photo> photoList;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        // 获取传递过来的照片列表和位置
        photoList = (ArrayList<Photo>) getIntent().getSerializableExtra(EXTRA_PHOTO_LIST);
        currentPosition = getIntent().getIntExtra(EXTRA_PHOTO_POSITION, 0);

        if (photoList == null || photoList.isEmpty()) {
            finish();
            return;
        }

        initViews();
        setupViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerPhoto);
    }

    private void setupViewPager() {
        adapter = new PhotoPagerAdapter(photoList);
        viewPager.setAdapter(adapter);

        // 设置当前显示的照片位置
        viewPager.setCurrentItem(currentPosition, false);

        // 注册页面变化回调（可选）
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
            }
        });
    }

    /**
     * PhotoAdapter - ViewPager2的Adapter
     */
    private static class PhotoPagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {

        private final List<Photo> photos;

        public PhotoPagerAdapter(List<Photo> photos) {
            this.photos = photos != null ? photos : new ArrayList<>();
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo_detail, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            if (position >= photos.size()) {
                return;
            }

            Photo photo = photos.get(position);
            String imageUrl = photo.getImageUrl();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .fitCenter()
                        .into(holder.imageView);
            }
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        static class PhotoViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView imageView;

            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageViewPhotoDetail);

                // 点击图片关闭Activity
                imageView.setOnClickListener(v -> {
                    if (itemView.getContext() instanceof AppCompatActivity) {
                        ((AppCompatActivity) itemView.getContext()).finish();
                    }
                });
            }
        }
    }
}
