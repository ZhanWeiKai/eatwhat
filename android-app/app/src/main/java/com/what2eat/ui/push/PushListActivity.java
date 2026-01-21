package com.what2eat.ui.push;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.data.model.Push;
import com.what2eat.ui.menu.MenuActivity;
import com.what2eat.ui.push.adapter.PushAdapter;
import com.what2eat.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 推送记录列表Activity
 */
public class PushListActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvPushes;
    private LinearLayout emptyState;
    private Button btnStartOrder;

    private PushAdapter pushAdapter;
    private List<Push> pushes;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_list);

        initViews();
        initAdapter();
        setListeners();
        loadPushes();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvPushes = findViewById(R.id.rvPushes);
        emptyState = findViewById(R.id.emptyState);
        btnStartOrder = findViewById(R.id.btnStartOrder);

        pushes = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);

        rvPushes.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        String currentUserId = RetrofitClient.getUserId(this);
        pushAdapter = new PushAdapter(this, currentUserId, push -> {
            // 删除确认对话框
            new AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage("确定要删除这条推送吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        deletePush(push);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        rvPushes.setAdapter(pushAdapter);
    }

    /**
     * 设置监听器
     */
    private void setListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 开始点菜按钮
        btnStartOrder.setOnClickListener(v -> {
            Intent intent = new Intent(PushListActivity.this, MenuActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 加载推送列表
     */
    private void loadPushes() {
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getAllPushes().enqueue(new Callback<ApiResponse<List<Push>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Push>>> call,
                                   Response<ApiResponse<List<Push>>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    pushes = response.body().getData();
                    if (pushes == null) {
                        pushes = new ArrayList<>();
                    }
                    pushAdapter.setPushes(pushes);

                    // 显示或隐藏空状态
                    if (pushes.isEmpty()) {
                        rvPushes.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvPushes.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载失败";
                    showError(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Push>>> call, Throwable t) {
                progressDialog.dismiss();
                showError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 删除推送
     */
    private void deletePush(Push push) {
        progressDialog.setMessage("删除中...");
        progressDialog.show();

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.deletePush(RetrofitClient.getToken(this), push.getPushId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        progressDialog.dismiss();

                        if (response.body() != null && response.body().isSuccess()) {
                            // 从列表中移除
                            pushAdapter.removePush(push);

                            // 检查是否为空
                            if (pushes.isEmpty()) {
                                rvPushes.setVisibility(View.GONE);
                                emptyState.setVisibility(View.VISIBLE);
                            }

                            showToast("删除成功");
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : "删除失败";
                            showError(message);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        progressDialog.dismiss();
                        showError("网络错误: " + t.getMessage());
                    }
                });
    }

    /**
     * 显示错误信息
     */
    private void showError(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示提示信息
     */
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
