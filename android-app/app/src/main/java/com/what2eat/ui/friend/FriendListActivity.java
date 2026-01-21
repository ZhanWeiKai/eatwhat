package com.what2eat.ui.friend;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.data.model.FriendDTO;
import com.what2eat.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 好友列表页面
 */
public class FriendListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnBack;
    private FriendAdapter adapter;
    private List<FriendDTO> friendList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        initViews();
        loadFriendList();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendAdapter(friendList);
        recyclerView.setAdapter(adapter);

        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadFriendList() {
        // 获取当前用户ID
        String userId = getSharedPreferences("What2Eat", MODE_PRIVATE)
                .getString("userId", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 调用API获取好友列表
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getFriendList(userId).enqueue(new Callback<ApiResponse<List<FriendDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FriendDTO>>> call,
                    Response<ApiResponse<List<FriendDTO>>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    List<FriendDTO> friends = response.body().getData();
                    if (friends != null) {
                        friendList.clear();
                        friendList.addAll(friends);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(FriendListActivity.this,
                            response.body() != null ? response.body().getMessage() : "加载失败",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FriendDTO>>> call, Throwable t) {
                Toast.makeText(FriendListActivity.this, "网络错误: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
