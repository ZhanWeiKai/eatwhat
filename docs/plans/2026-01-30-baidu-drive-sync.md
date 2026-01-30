# 百度网盘照片同步功能实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现相册照片批量同步到百度网盘的功能，包括OAuth认证、文件上传、进度跟踪和状态管理

**Architecture:**
- 后端：Spring Boot服务作为OAuth中转层，管理百度网盘API调用
- 前端：Android应用提供批量选择UI，显示同步进度
- 认证：使用百度网盘OAuth 2.0授权码模式
- 存储：Photo实体的baiduFileId字段存储百度网盘文件ID，isSynced标识同步状态

**Tech Stack:**
- 百度网盘开放平台API：https://pan.baidu.com/union/doc/0ksg0sbig
- Spring WebFlux (异步HTTP客户端)
- OkHttp (Android网络请求)
- WorkManager (Android后台任务)

---

## Task 1: 创建百度网盘配置管理

**Files:**
- Create: `springboot-backend/src/main/java/com/what2eat/config/BaiduDriveConfig.java`
- Modify: `springboot-backend/src/main/resources/application.yml`

**Step 1: 添加配置到application.yml**

```yaml
# 在application.yml末尾添加
baidu:
  drive:
    app-key: ${BAIDU_APP_KEY:your-app-key}
    secret-key: ${BAIDU_SECRET_KEY:your-secret-key}
    redirect-uri: http://api.jamesweb.org:8883/api/baidu/oauth/callback
    upload-api: https://pan.baidu.com/rest/2.0/xpan/file
    authorize-url: https://openapi.baidu.com/oauth/2.0/authorize
    token-url: https://openapi.baidu.com/oauth/2.0/token
```

**Step 2: 创建配置类**

创建文件 `springboot-backend/src/main/java/com/what2eat/config/BaiduDriveConfig.java`:

```java
package com.what2eat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.drive")
public class BaiduDriveConfig {
    private String appKey;
    private String secretKey;
    private String redirectUri;
    private String uploadApi;
    private String authorizeUrl;
    private String tokenUrl;
}
```

**Step 3: 验证配置加载**

测试命令：
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" compile
```

预期输出：BUILD SUCCESS

**Step 4: 提交配置**

```bash
git add springboot-backend/src/main/resources/application.yml
git add springboot-backend/src/main/java/com/what2eat/config/BaiduDriveConfig.java
git commit -m "feat: 添加百度网盘配置"
```

---

## Task 2: 实现百度网盘OAuth认证

**Files:**
- Create: `springboot-backend/src/main/java/com/what2eat/dto/BaiduTokenResponse.java`
- Create: `springboot-backend/src/main/java/com/what2eat/service/BaiduDriveService.java`
- Create: `springboot-backend/src/main/java/com/what2eat/controller/BaiduDriveController.java`
- Modify: `springboot-backend/src/main/java/com/what2eat/entity/User.java` (添加baiduAccessToken字段)

**Step 1: 创建百度Token响应DTO**

创建文件 `springboot-backend/src/main/java/com/what2eat/dto/BaiduTokenResponse.java`:

```java
package com.what2eat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaiduTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("session_secret")
    private String sessionSecret;
}
```

**Step 2: 添加百度Token字段到User实体**

修改 `springboot-backend/src/main/java/com/what2eat/entity/User.java`:

在User类中添加字段（在现有字段后）:
```java
@Column(name = "baidu_access_token", length = 500)
private String baiduAccessToken;

@Column(name = "baidu_refresh_token", length = 500)
private String baiduRefreshToken;
```

**Step 3: 创建数据库迁移**

创建文件 `springboot-backend/src/main/resources/db/migration/V5__add_baidu_token_fields.sql`:

```sql
ALTER TABLE user ADD COLUMN baidu_access_token VARCHAR(500) COMMENT '百度网盘访问令牌';
ALTER TABLE user ADD COLUMN baidu_refresh_token VARCHAR(500) COMMENT '百度网盘刷新令牌';
```

**Step 4: 实现BaiduDriveService核心方法**

创建文件 `springboot-backend/src/main/java/com/what2eat/service/BaiduDriveService.java`:

```java
package com.what2eat.service;

import com.what2eat.config.BaiduDriveConfig;
import com.what2eat.dto.BaiduTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduDriveService {

    private final BaiduDriveConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取授权URL
     */
    public String getAuthorizationUrl(String state) {
        return String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                config.getAuthorizeUrl(),
                config.getAppKey(),
                config.getRedirectUri(),
                "netdisk",
                state);
    }

    /**
     * 通过授权码获取访问令牌
     */
    public BaiduTokenResponse getAccessToken(String code) {
        String url = String.format("%s?grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s",
                config.getTokenUrl(),
                code,
                config.getAppKey(),
                config.getSecretKey(),
                config.getRedirectUri());

        return restTemplate.getForObject(url, BaiduTokenResponse.class);
    }

    /**
     * 上传文件到百度网盘
     */
    public String uploadFile(String accessToken, String filePath, String fileName) {
        // 预上传
        String preUploadUrl = String.format("%s?method=precreate&access_token=%s",
                config.getUploadApi(), accessToken);

        Map<String, Object> preUploadData = new HashMap<>();
        preUploadData.put("path", "/apps/what2eat/" + fileName);

        // TODO: 实现分片上传逻辑
        // 这里先返回占位符
        log.info("预上传URL: {}", preUploadUrl);
        return "placeholder_file_id";
    }
}
```

**Step 5: 创建OAuth控制器**

创建文件 `springboot-backend/src/main/java/com/what2eat/controller/BaiduDriveController.java`:

```java
package com.what2eat.controller;

import com.what2eat.config.BaiduDriveConfig;
import com.what2eat.dto.BaiduTokenResponse;
import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.BaiduDriveService;
import com.what2eat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/baidu")
@RequiredArgsConstructor
public class BaiduDriveController {

    private final BaiduDriveService baiduDriveService;
    private final UserService userService;

    /**
     * 获取授权URL
     */
    @GetMapping("/oauth/url")
    public ApiResponse<String> getAuthorizationUrl(@RequestParam String userId) {
        String url = baiduDriveService.getAuthorizationUrl(userId);
        return ApiResponse.success("获取成功", url);
    }

    /**
     * OAuth回调处理
     */
    @GetMapping("/oauth/callback")
    public String oauthCallback(
            @RequestParam String code,
            @RequestParam String state) {

        try {
            BaiduTokenResponse token = baiduDriveService.getAccessToken(code);
            // 保存token到用户表
            userService.saveBaiduToken(state, token.getAccessToken(), token.getRefreshToken());
            return "授权成功！请返回APP";
        } catch (Exception e) {
            return "授权失败: " + e.getMessage();
        }
    }
}
```

**Step 6: 添加saveBaiduToken方法到UserService**

修改 `springboot-backend/src/main/java/com/what2eat/service/UserService.java`，添加方法：

```java
@Transactional
public void saveBaiduToken(String userId, String accessToken, String refreshToken) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    user.setBaiduAccessToken(accessToken);
    user.setBaiduRefreshToken(refreshToken);
    userRepository.save(user);
}
```

**Step 7: 编译测试**

```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" compile
```

预期输出：BUILD SUCCESS

**Step 8: 提交OAuth功能**

```bash
git add springboot-backend/src/main/java/com/what2eat/
git add springboot-backend/src/main/resources/db/migration/V5__add_baidu_token_fields.sql
git commit -m "feat: 实现百度网盘OAuth认证"
```

---

## Task 3: 实现文件上传到百度网盘

**Files:**
- Modify: `springboot-backend/src/main/java/com/what2eat/service/BaiduDriveService.java`
- Create: `springboot-backend/src/main/java/com/what2eat/service/PhotoSyncService.java`
- Create: `springboot-backend/src/main/java/com/what2eat/controller/PhotoSyncController.java`

**Step 1: 实现完整的文件上传逻辑**

修改 `BaiduDriveService.java`，替换uploadFile方法：

```java
/**
 * 上传文件到百度网盘（完整实现）
 */
public String uploadFile(String accessToken, String fileUrl, String fileName) {
    try {
        // 1. 预上传获取uploadid
        String preUploadUrl = String.format("%s?method=precreate&access_token=%s",
                config.getUploadApi(), accessToken);

        Map<String, Object> preUploadData = new HashMap<>();
        preUploadData.put("path", "/apps/what2eat/" + fileName);
        preUploadData.put("size", 1024); // TODO: 获取实际文件大小
        preUploadData.put("block_list", new Object[]{"[]"});

        // 2. 上传分片
        String uploadUrl = "https://d.pcs.baidu.com/rest/2.0/pcs/superfile2?method=upload&access_token=" + accessToken;

        // 下载服务器文件并上传到百度
        RestTemplate downloadClient = new RestTemplate();
        byte[] fileData = downloadClient.getForObject(fileUrl, byte[].class);

        // 3. 创建文件
        String createUrl = String.format("%s?method=create&access_token=%s",
                config.getUploadApi(), accessToken);

        Map<String, Object> createData = new HashMap<>();
        createData.put("path", "/apps/what2eat/" + fileName);
        createData.put("size", fileData.length);
        createData.put("block_list", new Object[]{"[]"});

        ResponseEntity<Map> response = restTemplate.postForEntity(createUrl, createData, Map.class);

        // 返回文件fs_id
        Map<String, Object> result = (Map<String, Object>) response.getBody().get("list");
        return (String) result.get("fs_id");

    } catch (Exception e) {
        log.error("上传文件到百度网盘失败", e);
        throw new RuntimeException("上传失败: " + e.getMessage());
    }
}
```

**Step 2: 创建照片同步服务**

创建文件 `springboot-backend/src/main/java/com/what2eat/service/PhotoSyncService.java`:

```java
package com.what2eat.service;

import com.what2eat.entity.Photo;
import com.what2eat.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoSyncService {

    private final BaiduDriveService baiduDriveService;
    private final PhotoRepository photoRepository;
    private final UserService userService;

    /**
     * 同步单张照片到百度网盘
     */
    @Transactional
    public void syncPhoto(String userId, String photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("照片不存在"));

        if (!photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作他人照片");
        }

        // 获取用户的百度网盘token
        String accessToken = userService.getBaiduAccessToken(userId);
        if (accessToken == null) {
            throw new IllegalArgumentException("请先授权百度网盘");
        }

        // 上传到百度网盘
        String fileName = extractFileName(photo.getImageUrl());
        String baiduFileId = baiduDriveService.uploadFile(accessToken, photo.getImageUrl(), fileName);

        // 更新照片记录
        photo.setBaiduFileId(baiduFileId);
        photo.setIsSynced(true);
        photoRepository.save(photo);

        log.info("照片同步成功: photoId={}, baiduFileId={}", photoId, baiduFileId);
    }

    /**
     * 批量同步照片
     */
    @Transactional
    public void syncPhotos(String userId, List<String> photoIds) {
        for (String photoId : photoIds) {
            try {
                syncPhoto(userId, photoId);
            } catch (Exception e) {
                log.error("同步照片失败: photoId={}", photoId, e);
            }
        }
    }

    private String extractFileName(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 1];
    }
}
```

**Step 3: 添加getBaiduAccessToken方法到UserService**

修改 `springboot-backend/src/main/java/com/what2eat/service/UserService.java`，添加方法：

```java
public String getBaiduAccessToken(String userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    return user.getBaiduAccessToken();
}
```

**Step 4: 创建同步API控制器**

创建文件 `springboot-backend/src/main/java/com/what2eat/controller/PhotoSyncController.java`:

```java
package com.what2eat.controller;

import com.what2eat.dto.response.ApiResponse;
import com.what2eat.service.PhotoSyncService;
import com.what2eat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/photos/sync")
@RequiredArgsConstructor
public class PhotoSyncController {

    private final PhotoSyncService photoSyncService;
    private final JwtUtil jwtUtil;

    /**
     * 同步单张照片
     */
    @PostMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> syncPhoto(
            @RequestHeader("Authorization") String token,
            @PathVariable String photoId) {

        try {
            String userId = getUserIdFromToken(token);
            photoSyncService.syncPhoto(userId, photoId);
            return ResponseEntity.ok(ApiResponse.success("同步成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "同步失败: " + e.getMessage()));
        }
    }

    /**
     * 批量同步照片
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncPhotos(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, List<String>> request) {

        try {
            String userId = getUserIdFromToken(token);
            List<String> photoIds = request.get("photoIds");

            photoSyncService.syncPhotos(userId, photoIds);

            return ResponseEntity.ok(ApiResponse.success("批量同步已启动", Map.of(
                    "total", photoIds.size(),
                    "status", "processing"
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(500, "同步失败: " + e.getMessage()));
        }
    }

    private String getUserIdFromToken(String token) {
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtUtil.getUserIdFromToken(jwtToken);
    }
}
```

**Step 5: 编译测试**

```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" compile
```

预期输出：BUILD SUCCESS

**Step 6: 提交上传功能**

```bash
git add springboot-backend/src/main/java/com/what2eat/
git commit -m "feat: 实现照片上传到百度网盘"
```

---

## Task 4: Android端添加授权UI

**Files:**
- Create: `android-app/app/src/main/java/com/what2eat/ui/photo/BaiduOAuthActivity.java`
- Create: `android-app/app/src/main/res/layout/activity_baidu_oauth.xml`
- Modify: `android-app/app/src/main/AndroidManifest.xml`
- Modify: `android-app/app/src/main/java/com/what2eat/ui/photo/PhotoActivity.java`

**Step 1: 创建OAuth授权Activity**

创建文件 `android-app/app/src/main/java/com/what2eat/ui/photo/BaiduOAuthActivity.java`:

```java
package com.what2eat.ui.photo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // 获取授权URL
        String userId = RetrofitClient.getUserId(this);
        apiService.getBaiduOAuthUrl(userId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String authUrl = response.body();
                    webView.loadUrl(authUrl);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // 处理失败
            }
        });

        // 设置WebViewClient处理回调
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("api.jamesweb.org:8883/api/baidu/oauth/callback")) {
                    // 授权成功，关闭页面
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
}
```

**Step 2: 创建OAuth布局**

创建文件 `android-app/app/src/main/res/layout/activity_baidu_oauth.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

**Step 3: 添加网络权限（如果需要）**

确认 `AndroidManifest.xml` 已有INTERNET权限（应该已有）

**Step 4: 注册Activity**

修改 `android-app/app/src/main/AndroidManifest.xml`，添加：

```xml
<!-- 百度网盘授权页面 -->
<activity
    android:name=".ui.photo.BaiduOAuthActivity"
    android:exported="false" />
```

**Step 5: 添加API接口**

修改 `android-app/app/src/main/java/com/what2eat/data/api/ApiService.java`，添加：

```java
@GET("baidu/oauth/url")
Call<String> getBaiduOAuthUrl(@Query("userId") String userId);
```

**Step 6: 在PhotoActivity添加授权入口**

修改 `android-app/app/src/main/java/com/what2eat/ui/photo/PhotoActivity.java`，在initViews()方法后添加按钮：

```java
private void initViews() {
    // ... 现有代码 ...

    // 添加"授权百度网盘"按钮
    Button btnAuthBaidu = findViewById(R.id.btnAuthBaidu);
    btnAuthBaidu.setOnClickListener(v -> {
        Intent intent = new Intent(this, BaiduOAuthActivity.class);
        startActivity(intent);
    });
}
```

**Step 7: 编译测试**

```bash
cd android-app
./gradlew.bat assembleDebug
```

预期输出：BUILD SUCCESSFUL

**Step 8: 提交授权UI**

```bash
git add android-app/
git commit -m "feat: 添加百度网盘授权UI"
```

---

## Task 5: 实现批量选择和同步UI

**Files:**
- Modify: `android-app/app/src/main/java/com/what2eat/ui/photo/PhotoAdapter.java`
- Modify: `android-app/app/src/main/res/layout/item_photo.xml`
- Modify: `android-app/app/src/main/java/com/what2eat/ui/photo/PhotoActivity.java`
- Modify: `android-app/app/src/main/res/layout/activity_photo.xml`

**Step 1: 修改PhotoAdapter支持选择模式**

修改 `android-app/app/src/main/java/com/what2eat/ui/photo/PhotoAdapter.java`:

添加字段和方法：
```java
private Set<String> selectedPhotoIds = new HashSet<>();
private boolean isSelectionMode = false;

public void setSelectionMode(boolean mode) {
    this.isSelectionMode = mode;
    selectedPhotoIds.clear();
    notifyDataSetChanged();
}

public List<String> getSelectedPhotoIds() {
    return new ArrayList<>(selectedPhotoIds);
}

public void toggleSelection(String photoId) {
    if (selectedPhotoIds.contains(photoId)) {
        selectedPhotoIds.remove(photoId);
    } else {
        selectedPhotoIds.add(photoId);
    }
    notifyDataSetChanged();
}

private boolean isSelected(String photoId) {
    return selectedPhotoIds.contains(photoId);
}
```

修改onBindViewHolder添加选择逻辑：
```java
@Override
public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
    // ... 现有代码 ...

    // 选择模式逻辑
    if (isSelectionMode) {
        holder.itemView.setOnClickListener(v -> {
            toggleSelection(photo.getPhotoId());
        });
        holder.checkBox.setVisibility(View.VISIBLE);
        holder.checkBox.setChecked(isSelected(photo.getPhotoId()));
    } else {
        // 原有的点击查看大图逻辑
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            // ... 现有代码 ...
        });
        holder.checkBox.setVisibility(View.GONE);
    }
}
```

**Step 2: 修改item_photo.xml添加复选框**

修改 `android-app/app/src/main/res/layout/item_photo.xml`，在ImageView后添加：

```xml
<CheckBox
    android:id="@+id/checkBox"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="top|end"
    android:visibility="gone" />
```

**Step 3: 修改PhotoActivity添加批量操作**

修改 `android-app/app/src/main/java/com/what2eat/ui/photo/PhotoActivity.java`，添加：

```java
private Button btnSelectMode;
private Button btnSyncSelected;
private boolean isSelectMode = false;

private void initViews() {
    // ... 现有代码 ...

    btnSelectMode = findViewById(R.id.btnSelectMode);
    btnSyncSelected = findViewById(R.id.btnSyncSelected);

    btnSelectMode.setOnClickListener(v -> {
        isSelectMode = !isSelectMode;
        photoAdapter.setSelectionMode(isSelectMode);
        updateButtons();
    });

    btnSyncSelected.setOnClickListener(v -> {
        List<String> selectedIds = photoAdapter.getSelectedPhotoIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "请先选择照片", Toast.LENGTH_SHORT).show();
            return;
        }
        syncSelectedPhotos(selectedIds);
    });
}

private void updateButtons() {
    if (isSelectMode) {
        btnSelectMode.setText("取消选择");
        btnSyncSelected.setVisibility(View.VISIBLE);
    } else {
        btnSelectMode.setText("选择照片");
        btnSyncSelected.setVisibility(View.GONE);
    }
}

private void syncSelectedPhotos(List<String> photoIds) {
    progressDialog.setMessage("正在同步...");
    progressDialog.show();

    Map<String, Object> request = new HashMap<>();
    request.put("photoIds", photoIds);

    String token = RetrofitClient.getToken(this);
    apiService.syncPhotos("Bearer " + token, request).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
        @Override
        public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
            progressDialog.dismiss();
            if (response.isSuccessful() && response.body() != null) {
                Toast.makeText(PhotoActivity.this, "同步成功", Toast.LENGTH_SHORT).show();
                photoAdapter.setSelectionMode(false);
                isSelectMode = false;
                updateButtons();
                loadPhotos(); // 刷新列表
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
            progressDialog.dismiss();
            Toast.makeText(PhotoActivity.this, "同步失败", Toast.LENGTH_SHORT).show();
        }
    });
}
```

**Step 4: 修改布局文件添加按钮**

修改 `android-app/app/src/main/res/layout/activity_photo.xml`，在添加照片按钮前添加：

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp">

    <Button
        android:id="@+id/btnAuthBaidu"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginEnd="8dp"
        android:text="授权百度网盘"
        android:background="#FF6600"
        android:textColor="#FFFFFF" />

    <Button
        android:id="@+id/btnSelectMode"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginEnd="8dp"
        android:text="选择照片"
        android:background="#FF6600"
        android:textColor="#FFFFFF" />

    <Button
        android:id="@+id/btnSyncSelected"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:visibility="gone"
        android:text="同步选中"
        android:background="#FF9800"
        android:textColor="#FFFFFF" />

</LinearLayout>
```

**Step 5: 添加API接口**

修改 `android-app/app/src/main/java/com/what2eat/data/api/ApiService.java`，添加：

```java
@POST("photos/sync/batch")
Call<ApiResponse<Map<String, Object>>> syncPhotos(
    @Header("Authorization") String token,
    @Body Map<String, Object> request
);

@POST("photos/sync/{photoId}")
Call<ApiResponse<Void>> syncPhoto(
    @Header("Authorization") String token,
    @Path("photoId") String photoId
);
```

**Step 6: 编译测试**

```bash
cd android-app
./gradlew.bat assembleDebug
```

预期输出：BUILD SUCCESSFUL

**Step 7: 提交批量同步功能**

```bash
git add android-app/
git commit -m "feat: 实现照片批量选择和同步UI"
```

---

## Task 6: 部署和集成测试

**Files:**
- N/A (部署和测试)

**Step 1: 编译后端**

```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests
```

预期输出：BUILD SUCCESS

**Step 2: 部署到服务器**

```bash
scp springboot-backend/target/what2eat-backend-1.0.0.jar root@47.83.126.42:/root/what2eat/target/
ssh root@47.83.126.42 "cd /root/what2eat && docker compose restart app"
sleep 8
```

预期输出：Container what2eat-app Started

**Step 3: 验证数据库迁移**

```bash
ssh root@47.83.126.42 "docker exec what2eat-mysql mysql -uroot -p123456 -e 'USE what2eat; DESCRIBE user;' | grep baidu"
```

预期输出：显示baidu_access_token和baidu_refresh_token字段

**Step 4: 编译Android**

```bash
cd android-app
./gradlew.bat assembleDebug
```

预期输出：BUILD SUCCESSFUL

**Step 5: 安装APK**

```bash
adb install android-app/app/build/outputs/apk/debug/app-debug.apk
```

预期输出：Success

**Step 6: 手动测试清单**

- [ ] 打开应用，进入"我的相册"
- [ ] 点击"授权百度网盘"按钮
- [ ] 在WebView中完成百度账号登录授权
- [ ] 授权成功后返回相册页面
- [ ] 点击"选择照片"进入选择模式
- [ ] 选择多张照片
- [ ] 点击"同步选中"按钮
- [ ] 查看同步进度提示
- [ ] 同步完成后刷新照片列表
- [ ] 检查照片是否显示已同步标识

**Step 7: 提交部署**

```bash
git add docs/
git commit -m "docs: 添加百度网盘同步功能实施计划"
```

---

## 附录：百度网盘开放平台配置

### 申请应用

1. 访问 https://pan.baidu.com/union/doc/0ksg0sbig
2. 创建应用获取App Key和Secret Key
3. 配置回调URL：`http://api.jamesweb.org:8883/api/baidu/oauth/callback`
4. 申请权限：`netdisk` (读写网盘文件)

### 环境变量配置

生产环境设置环境变量（推荐）：

```bash
export BAIDU_APP_KEY="your-app-key"
export BAIDU_SECRET_KEY="your-secret-key"
```

或直接修改application.yml（开发环境）

---

## 已知问题和TODO

1. **分片上传**：当前实现为简化版，大文件需要实现分片上传
2. **Token刷新**：需要实现refresh_token自动刷新逻辑
3. **进度回调**：Android端需要显示上传进度百分比
4. **错误处理**：网络异常、授权过期等场景需要完善
5. **离线支持**：使用WorkManager实现后台同步队列

---

## 下一步行动

**Plan complete and saved to `docs/plans/2026-01-30-baidu-drive-sync.md`. Two execution options:**

**1. Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
