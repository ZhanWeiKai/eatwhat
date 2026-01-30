package com.what2eat.ui.photo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.what2eat.R;
import com.what2eat.data.api.ApiService;
import com.what2eat.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 百度网盘OAuth授权Activity
 */
public class BaiduOAuthActivity extends AppCompatActivity {

    private WebView webView;
    private ApiService apiService;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_oauth);

        webView = findViewById(R.id.webView);
        apiService = RetrofitClient.getApiService(this);

        // 配置WebView
        setupWebView();

        // 获取授权URL并加载
        loadAuthorizationUrl();
    }

    /**
     * 配置WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // 设置WebViewClient处理回调
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 检查是否是OAuth回调
                if (url.contains("api.jamesweb.org:8883/api/baidu/oauth/callback")) {
                    // OAuth回调，授权成功
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 检查页面标题是否包含"授权成功"
                if (view.getTitle() != null && view.getTitle().contains("授权成功")) {
                    Toast.makeText(BaiduOAuthActivity.this, "百度网盘授权成功", Toast.LENGTH_SHORT).show();
                    // 延迟关闭页面
                    view.postDelayed(() -> finish(), 2000);
                }
            }
        });
    }

    /**
     * 加载授权URL
     */
    private void loadAuthorizationUrl() {
        String userId = RetrofitClient.getUserId(this);

        if (userId == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService.getBaiduOAuthUrl(userId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body();
                    webView.loadUrl(authUrl);
                } else {
                    Toast.makeText(BaiduOAuthActivity.this, "获取授权链接失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(BaiduOAuthActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 如果WebView可以后退，则后退而不是关闭Activity
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
