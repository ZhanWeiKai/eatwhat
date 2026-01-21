package com.what2eat.ui.qrcode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.utils.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 扫描二维码Activity
 * 扫描好友二维码并添加好友
 */
public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private DecoratedBarcodeView barcodeView;
    private ImageButton btnBack;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // 获取当前用户ID
        currentUserId = RetrofitClient.getUserId(this);

        // 初始化视图
        initViews();

        // 检查相机权限
        checkCameraPermission();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        barcodeView = findViewById(R.id.barcodeView);
        btnBack = findViewById(R.id.btnBack);

        // 设置返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 设置扫描回调
        barcodeView.decodeContinuous(result -> {
            // 扫描成功，暂停扫描
            barcodeView.pause();
            // 处理扫描结果
            handleScanResult(result);
        });
    }

    /**
     * 检查相机权限
     */
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // 请求相机权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            // 已有权限，启动扫描
            startScanning();
        }
    }

    /**
     * 启动扫描
     */
    private void startScanning() {
        barcodeView.resume();
    }

    /**
     * 处理扫描结果
     */
    private void handleScanResult(BarcodeResult result) {
        String content = result.getText();
        if (content == null || content.isEmpty()) {
            Toast.makeText(this, "扫描失败，请重试", Toast.LENGTH_SHORT).show();
            barcodeView.resume();
            return;
        }

        // 解析出好友ID
        String friendId = content.trim();

        // 检查是否扫描的是自己
        if (friendId.equals(currentUserId)) {
            Toast.makeText(this, "不能添加自己为好友", Toast.LENGTH_SHORT).show();
            barcodeView.resume();
            return;
        }

        // 显示好友信息对话框
        showFriendDialog(friendId);
    }

    /**
     * 显示好友信息对话框
     */
    private void showFriendDialog(String friendId) {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scan_result, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        // 初始化视图
        ImageView ivAvatar = dialogView.findViewById(R.id.ivAvatar);
        TextView tvNickname = dialogView.findViewById(R.id.tvNickname);
        TextView tvUserId = dialogView.findViewById(R.id.tvUserId);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAddFriend = dialogView.findViewById(R.id.btnAddFriend);

        // 显示好友信息（暂时只显示ID，昵称需要从后端获取）
        tvUserId.setText("ID: " + friendId);
        tvNickname.setText("用户" + friendId.substring(0, Math.min(6, friendId.length())));

        // 取消按钮
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            // 继续扫描
            barcodeView.resume();
        });

        // 添加好友按钮
        btnAddFriend.setOnClickListener(v -> {
            dialog.dismiss();
            // 调用API添加好友
            addFriend(friendId);
        });

        // 显示对话框
        dialog.show();
    }

    /**
     * 调用API添加好友
     */
    private void addFriend(String friendId) {
        // 显示加载提示
        Toast.makeText(this, "正在添加好友...", Toast.LENGTH_SHORT).show();

        // 获取Token
        String token = RetrofitClient.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 调用API
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.addFriend("Bearer " + token, friendId).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                                   Response<ApiResponse<Map<String, String>>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    // 添加成功
                    Toast.makeText(ScanActivity.this, "添加好友成功！", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // 添加失败
                    String message = response.body() != null ? response.body().getMessage() : "添加失败";
                    Toast.makeText(ScanActivity.this, message, Toast.LENGTH_SHORT).show();
                    // 继续扫描
                    barcodeView.resume();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Toast.makeText(ScanActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // 继续扫描
                barcodeView.resume();
            }
        });
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予，启动扫描
                startScanning();
            } else {
                // 权限拒绝
                Toast.makeText(this, "需要相机权限才能扫描二维码", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}
