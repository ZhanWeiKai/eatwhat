package com.what2eat.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.data.model.ApiResponse;
import com.what2eat.ui.main.MainActivity;
import com.what2eat.utils.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 登录/注册Activity
 */
public class LoginActivity extends AppCompatActivity {

    private LinearLayout loginForm;
    private LinearLayout registerForm;
    private Button btnLoginTab;
    private Button btnRegisterTab;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etRegUsername;
    private TextInputEditText etRegNickname;
    private TextInputEditText etRegPassword;
    private TextInputEditText etRegConfirmPassword;
    private Button btnLogin;
    private Button btnRegister;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 检查是否已登录
        if (RetrofitClient.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        setListeners();
    }

    private void initViews() {
        loginForm = findViewById(R.id.loginForm);
        registerForm = findViewById(R.id.registerForm);
        btnLoginTab = findViewById(R.id.btnLoginTab);
        btnRegisterTab = findViewById(R.id.btnRegisterTab);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegNickname = findViewById(R.id.etRegNickname);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);
    }

    private void setListeners() {
        // 切换到登录表单
        btnLoginTab.setOnClickListener(v -> {
            loginForm.setVisibility(View.VISIBLE);
            registerForm.setVisibility(View.GONE);
        });

        // 切换到注册表单
        btnRegisterTab.setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
        });

        // 登录按钮
        btnLogin.setOnClickListener(v -> login());

        // 注册按钮
        btnRegister.setOnClickListener(v -> register());
    }

    /**
     * 登录
     */
    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 验证输入
        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // 构建请求参数
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        // 调用登录接口
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.login(request).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                                   Response<ApiResponse<Map<String, String>>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    Map<String, String> data = response.body().getData();
                    String token = data.get("token");
                    String userId = data.get("userId");
                    String nickname = data.get("nickname");

                    // 保存Token和用户信息
                    RetrofitClient.saveToken(LoginActivity.this, token);
                    RetrofitClient.saveUserInfo(LoginActivity.this, userId, username, nickname);

                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                    // 跳转到主页
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "登录失败";
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 注册
     */
    private void register() {
        String username = etRegUsername.getText().toString().trim();
        String nickname = etRegNickname.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        // 验证输入
        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3) {
            Toast.makeText(this, "用户名至少3个字符", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nickname.isEmpty()) {
            Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "密码至少6个字符", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // 构建请求参数
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("nickname", nickname);

        // 调用注册接口
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.register(request).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                                   Response<ApiResponse<Map<String, String>>> response) {
                progressDialog.dismiss();

                if (response.body() != null && response.body().isSuccess()) {
                    Map<String, String> data = response.body().getData();
                    String token = data.get("token");
                    String userId = data.get("userId");

                    // 保存Token和用户信息
                    RetrofitClient.saveToken(LoginActivity.this, token);
                    RetrofitClient.saveUserInfo(LoginActivity.this, userId, username, nickname);

                    Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

                    // 跳转到主页
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "注册失败";
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
