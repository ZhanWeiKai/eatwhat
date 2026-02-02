package com.what2eat.ui.ai;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.what2eat.R;
import com.what2eat.utils.RetrofitClient;

/**
 * AI聊天Activity - WebView版本
 * 使用HTML + SSE实现流式聊天
 */
public class AIChatActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat_webview);

        // 初始化WebView
        initWebView();

        // 加载HTML页面
        loadChatPage();
    }

    /**
     * 初始化WebView
     */
    private void initWebView() {
        webView = findViewById(R.id.webview);

        // 配置WebView设置
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);  // 启用JavaScript
        settings.setDomStorageEnabled(true);  // 启用DOM存储
        settings.setAllowFileAccess(true);    // 允许文件访问
        settings.setAllowContentAccess(true); // 允许内容访问

        // 【重要】允许混合内容（HTTPS页面加载HTTP图片）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 启用缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // 自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // 改善滚动性能
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        // 禁用缓存，确保每次都加载最新文件
        settings.setDatabaseEnabled(false);

        // 设置User Agent，避免缓存
        String userAgent = settings.getUserAgentString();
        settings.setUserAgentString(userAgent + "/What2Eat-Chat");

        // 设置WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 页面加载完成后，调用JS初始化
                initJavaScript();
            }
        });

        // 设置WebChromeClient以支持更多功能
        webView.setWebChromeClient(new WebChromeClient());
    }

    /**
     * 加载聊天页面
     */
    private void loadChatPage() {
        // 从assets目录加载HTML，添加时间戳避免缓存
        String url = "file:///android_asset/chat.html?t=" + System.currentTimeMillis();
        webView.loadUrl(url);
    }

    /**
     * 初始化JavaScript（传递用户信息）
     */
    private void initJavaScript() {
        // 获取用户ID和Token
        String userId = RetrofitClient.getUserId(this);
        String token = RetrofitClient.getToken(this);

        if (userId != null && token != null) {
            // 调用JS的init函数
            String js = String.format("javascript:init('%s', '%s')", userId, token);
            webView.evaluateJavascript(js, value -> {
                System.out.println("JavaScript初始化完成: " + value);
            });
        } else {
            // 未登录，跳转到登录页
            finish();
        }
    }

    /**
     * 返回键处理 - 返回上一页而不是退出
     */
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Activity销毁时清理WebView
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}
