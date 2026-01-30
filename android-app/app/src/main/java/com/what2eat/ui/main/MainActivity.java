package com.what2eat.ui.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.service.WebSocketService;
import com.what2eat.ui.friend.FriendListActivity;
import com.what2eat.ui.login.LoginActivity;
import com.what2eat.ui.menu.MenuActivity;
import com.what2eat.ui.photo.PhotoActivity;
import com.what2eat.ui.push.PushListActivity;
import com.what2eat.ui.qrcode.QRCodeActivity;
import com.what2eat.ui.qrcode.ScanActivity;
import com.what2eat.ui.upload.UploadActivity;
import com.what2eat.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 主页Activity
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvNickname;
    private Button btnLogout;
    private ApiService apiService;
    private ProgressDialog progressDialog;
    private CardView cardStartOrder;
    private CardView cardUpload;
    private CardView cardMyPushes;
    private CardView cardScan;
    private CardView cardMyQR;
    private CardView cardMyFriends;
    private CardView cardMyPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化API服务
        apiService = RetrofitClient.getApiService(this);

        // 初始化进度对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在退出...");
        progressDialog.setCancelable(false);

        // 启动WebSocket前台服务
        startWebSocketService();

        initViews();
        setListeners();
        loadUserInfo();
    }

    /**
     * 启动WebSocket前台服务
     */
    private void startWebSocketService() {
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
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
        cardMyFriends = findViewById(R.id.cardMyFriends);
        cardMyPhotos = findViewById(R.id.cardMyPhotos);
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

        // 我的好友
        cardMyFriends.setOnClickListener(v -> {
            startActivity(new Intent(this, FriendListActivity.class));
        });

        // 我的相册
        cardMyPhotos.setOnClickListener(v -> {
            startActivity(new Intent(this, PhotoActivity.class));
        });

        // 退出登录
        btnLogout.setOnClickListener(v -> {
            performLogout();
        });
    }

    /**
     * 执行退出登录操作
     */
    private void performLogout() {
        // 获取token
        String token = RetrofitClient.getToken(this);
        if (token == null) {
            // 没有token，直接清除数据并跳转登录页
            clearDataAndNavigateToLogin();
            return;
        }

        // 调用退出登录API
        progressDialog.show();
        apiService.logout("Bearer " + token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                progressDialog.dismiss();
                // 无论API调用成功与否，都清除本地数据
                clearDataAndNavigateToLogin();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                progressDialog.dismiss();
                // 网络失败，也清除本地数据
                clearDataAndNavigateToLogin();
            }
        });
    }

    /**
     * 清除本地数据并跳转到登录页面
     */
    private void clearDataAndNavigateToLogin() {
        // 清除token
        RetrofitClient.clearToken(this);

        // 清除SharedPreferences中的用户信息
        getSharedPreferences("What2Eat", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // 停止WebSocket服务
        stopService(new Intent(this, WebSocketService.class));

        // 跳转到登录页面，并清除Activity栈
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
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
