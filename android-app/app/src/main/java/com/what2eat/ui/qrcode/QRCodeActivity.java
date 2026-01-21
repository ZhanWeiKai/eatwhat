package com.what2eat.ui.qrcode;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.what2eat.R;
import com.what2eat.utils.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 我的二维码Activity
 * 显示用户的个人二维码，供好友扫描添加
 */
public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private ImageView ivQRCode;
    private TextView tvNickname;
    private TextView tvUserId;
    private Button btnShare;
    private Toolbar toolbar;

    private String userId;
    private String nickname;
    private String avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        // 初始化视图
        initViews();

        // 获取用户信息
        getUserInfo();

        // 显示用户信息
        displayUserInfo();

        // 生成二维码
        generateQRCode();

        // 设置事件监听
        setListeners();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvNickname = findViewById(R.id.tvNickname);
        tvUserId = findViewById(R.id.tvUserId);
        btnShare = findViewById(R.id.btnShare);

        // 设置标题栏
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * 从SharedPreferences获取用户信息
     */
    private void getUserInfo() {
        userId = RetrofitClient.getUserId(this);

        SharedPreferences prefs = getSharedPreferences("What2Eat", MODE_PRIVATE);
        nickname = prefs.getString("nickname", null);
        avatar = prefs.getString("avatar", null);
    }

    /**
     * 显示用户信息
     */
    private void displayUserInfo() {
        // 显示昵称
        if (nickname != null && !nickname.isEmpty()) {
            tvNickname.setText(nickname);
        } else {
            tvNickname.setText("用户");
        }

        // 显示用户ID
        if (userId != null && !userId.isEmpty()) {
            tvUserId.setText("ID: " + userId);
        } else {
            tvUserId.setText("ID: 未知");
            Toast.makeText(this, "用户信息加载失败", Toast.LENGTH_SHORT).show();
        }

        // 显示头像
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivAvatar);
        }
    }

    /**
     * 生成二维码
     */
    private void generateQRCode() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "用户ID为空，无法生成二维码", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 创建QRCodeWriter对象
            QRCodeWriter writer = new QRCodeWriter();

            // 设置二维码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // 生成二维码矩阵（尺寸：250x250）
            int width = 250;
            int height = 250;
            BitMatrix bitMatrix = writer.encode(userId, BarcodeFormat.QR_CODE, width, height, hints);

            // 创建位图
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // 绘制二维码
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // 黑色：0xFF000000，白色：0xFFFFFFFF
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // 显示二维码
            ivQRCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "二维码生成失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置事件监听
     */
    private void setListeners() {
        // 分享按钮（暂时显示提示）
        btnShare.setOnClickListener(v -> {
            Toast.makeText(this, "截图分享给好友", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 处理返回按钮点击
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
