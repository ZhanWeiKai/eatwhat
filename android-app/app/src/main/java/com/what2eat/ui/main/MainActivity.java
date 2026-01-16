package com.what2eat.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.what2eat.R;
import com.what2eat.ui.menu.MenuActivity;
import com.what2eat.ui.push.PushListActivity;
import com.what2eat.ui.qrcode.QRCodeActivity;
import com.what2eat.ui.qrcode.ScanActivity;
import com.what2eat.ui.upload.UploadActivity;
import com.what2eat.utils.RetrofitClient;

/**
 * 主页Activity
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvNickname;
    private Button btnLogout;
    private CardView cardStartOrder;
    private CardView cardUpload;
    private CardView cardMyPushes;
    private CardView cardScan;
    private CardView cardMyQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();
        loadUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    private void initViews() {
        tvNickname = findViewById(R.id.tvNickname);
        btnLogout = findViewById(R.id.btnLogout);
        cardStartOrder = findViewById(R.id.cardStartOrder);
        cardUpload = findViewById(R.id.cardUpload);
        cardMyPushes = findViewById(R.id.cardMyPushes);
        cardScan = findViewById(R.id.cardScan);
        cardMyQR = findViewById(R.id.cardMyQR);
    }

    private void setListeners() {
        // 开始点菜
        cardStartOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, MenuActivity.class));
        });

        // 上传菜品
        cardUpload.setOnClickListener(v -> {
            startActivity(new Intent(this, UploadActivity.class));
        });

        // 已推送菜单
        cardMyPushes.setOnClickListener(v -> {
            startActivity(new Intent(this, PushListActivity.class));
        });

        // 扫一扫
        cardScan.setOnClickListener(v -> {
            startActivity(new Intent(this, ScanActivity.class));
        });

        // 我的二维码
        cardMyQR.setOnClickListener(v -> {
            startActivity(new Intent(this, QRCodeActivity.class));
        });

        // 退出登录
        btnLogout.setOnClickListener(v -> {
            RetrofitClient.clearToken(this);
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * 加载用户信息
     */
    private void loadUserInfo() {
        String nickname = getSharedPreferences("What2Eat", MODE_PRIVATE)
                .getString("nickname", "用户");
        tvNickname.setText(nickname);
    }
}
