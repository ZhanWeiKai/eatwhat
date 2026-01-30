package com.what2eat.ui.ai;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ChatMessage;
import com.what2eat.data.model.ChatResponse;
import com.what2eat.utils.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText inputEditText;
    private ImageButton sendButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // 初始化视图
        initViews();

        // 初始化API
        apiService = RetrofitClient.getApiService(this);

        // 设置RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter();
        chatRecyclerView.setAdapter(chatAdapter);

        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        inputEditText = findViewById(R.id.input_edit_text);
        sendButton = findViewById(R.id.send_button);

        // 设置返回按钮
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void sendMessage() {
        String message = inputEditText.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        // 添加用户消息到列表
        ChatMessage userMessage = new ChatMessage(message, true);
        chatAdapter.addMessage(userMessage);
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

        // 清空输入框
        inputEditText.setText("");

        // 禁用发送按钮
        sendButton.setEnabled(false);

        // 添加AI消息占位符
        ChatMessage aiMessage = new ChatMessage("", false);
        chatAdapter.addMessage(aiMessage);

        // 调用AI API
        callAIAPI(message);
    }

    private void callAIAPI(String message) {
        Map<String, String> request = new HashMap<>();
        request.put("message", message);

        apiService.sendChatMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    if (chatResponse.isSuccess() && chatResponse.getData() != null) {
                        String reply = chatResponse.getData().getMessage();
                        chatAdapter.updateLastMessage(reply);
                    } else {
                        chatAdapter.updateLastMessage("抱歉，我现在无法回复。请稍后再试。");
                    }
                } else {
                    chatAdapter.updateLastMessage("服务器错误，请稍后再试。");
                }

                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                sendButton.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                t.printStackTrace();
                chatAdapter.updateLastMessage("网络错误: " + t.getMessage());
                sendButton.setEnabled(true);
            }
        });
    }
}
