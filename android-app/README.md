# 今天吃什么 - Android应用

## 项目概述

这是一个Android原生Java应用，用于"今天吃什么"点菜推送系统的客户端。

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 33+
- Gradle 8.2+
- 最低Android版本：Android 13 (API 33)
- 目标Android版本：Android 13 (API 33)

## 项目结构

```
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/what2eat/
│   │   │   ├── ui/                    # UI层
│   │   │   │   ├── login/             # 登录/注册
│   │   │   │   ├── main/              # 主页
│   │   │   │   ├── menu/              # 点菜
│   │   │   │   ├── push/              # 推送记录
│   │   │   │   ├── qrcode/            # 二维码
│   │   │   │   ├── upload/            # 上传菜品
│   │   │   │   └── profile/           # 个人信息
│   │   │   ├── data/                  # 数据层
│   │   │   │   ├── api/               # API接口
│   │   │   │   ├── model/             # 数据模型
│   │   │   │   ├── repository/        # 仓库
│   │   │   │   └── local/             # 本地存储
│   │   │   └── utils/                 # 工具类
│   │   └── res/                       # 资源文件
│   └── build.gradle.kts               # 应用级构建脚本
```

## 配置说明

### 1. 修改后端地址

编辑 `app/build.gradle.kts`，修改 `debug` 类型的 `BASE_URL`：

```kotlin
buildTypes {
    debug {
        // 真机测试：使用电脑的局域网IP（根据实际情况修改）
        // 先在电脑上执行 ipconfig 查看局域网IP，然后替换下面的IP
        buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8883/api/\"")
        buildConfigField("String", "WS_URL", "\"ws://192.168.1.100:8883/api/ws\"")
    }
}
```

### 2. 获取电脑的局域网IP

**Windows:**
```bash
ipconfig
# 查找 "无线局域网适配器 WLAN" 或 "以太网适配器" 下的 IPv4 地址
```

**Mac/Linux:**
```bash
ifconfig
# 查找 en0 下的 inet 地址
```

### 3. 网络连接要求

- **真机测试**：手机和电脑必须连接同一个WiFi
- **模拟器测试**：使用 `10.0.2.2` 地址，无需配置

## 如何运行

### 使用Android Studio

1. 打开Android Studio
2. 选择 "Open" → 选择 `android-app` 目录
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击 "Run" 按钮（或按 Shift+F10）

### 使用命令行

```bash
cd android-app
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 已实现功能

✅ **基础架构**
- 项目结构搭建
- 网络层配置（Retrofit + OkHttp）
- 数据模型（User, Dish, Push, ApiResponse）
- API接口定义
- Token管理（SharedPreferences）

✅ **后端集成**
- 完整的API接口定义
- 自动添加Token拦截器
- 网络日志（Debug模式）

## 待实现功能（UI和业务逻辑）

### 1. 登录/注册模块

**需要创建的文件：**
- `ui/login/LoginActivity.java` - 登录页面
- `ui/login/LoginViewModel.java` - 登录业务逻辑
- `res/layout/activity_login.xml` - 登录布局
- `res/layout/activity_register.xml` - 注册布局

**核心逻辑：**
```java
// 登录示例
Map<String, String> request = new HashMap<>();
request.put("username", username);
request.put("password", password);

RetrofitClient.getApiService(this).login(request).enqueue(new Callback<>() {
    @Override
    public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                           Response<ApiResponse<Map<String, String>>> response) {
        if (response.body().isSuccess()) {
            String token = response.body().getData().get("token");
            String userId = response.body().getData().get("userId");

            // 保存Token和用户信息
            RetrofitClient.saveToken(LoginActivity.this, token);
            RetrofitClient.saveUserInfo(LoginActivity.this, userId, username, nickname);

            // 跳转到主页
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
        // 处理错误
    }
});
```

### 2. 主页模块

**需要创建：**
- `ui/main/MainActivity.java`
- `res/layout/activity_main.xml`
- 功能入口按钮（点菜、上传、推送列表、二维码）

### 3. 点菜模块

**需要创建：**
- `ui/menu/MenuActivity.java`
- `ui/menu/DishAdapter.java` - 菜品列表适配器
- `ui/menu/CartAdapter.java` - 购物车适配器
- `res/layout/activity_menu.xml`
- `res/layout/item_dish.xml`
- `res/layout/item_cart.xml`

**核心功能：**
- 分类导航
- 菜品列表展示
- 添加到购物车
- 购物车管理
- 推送菜单

### 4. 推送记录模块

**需要创建：**
- `ui/push/PushListActivity.java`
- `ui/push/PushListAdapter.java`
- `res/layout/activity_push_list.xml`
- `res/layout/item_push.xml`

**核心功能：**
- 查看推送列表
- 权限控制（只能删除自己的推送）
- 删除推送

### 5. 二维码模块

**需要创建：**
- `ui/qrcode/QRCodeActivity.java` - 我的二维码
- `ui/qrcode/ScanActivity.java` - 扫描
- `res/layout/activity_qrcode.xml`

**核心功能：**
- 生成二维码（包含userId）
- 扫描二维码
- 解析friendId
- 添加好友

**使用ZXing示例：**
```java
// 生成二维码
QRCodeWriter writer = new QRCodeWriter();
BitMatrix bitMatrix = writer.encode(userId, BarcodeFormat.QR_CODE, 512, 512);

// 扫描二维码
new IntentIntegrator(this)
    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
    .setPrompt("扫描二维码")
    .initiateScan();
```

### 6. 上传菜品模块

**需要创建：**
- `ui/upload/UploadActivity.java`
- `res/layout/activity_upload.xml`

**核心功能：**
- 选择图片（相册/拍照）
- 图片预览
- 表单输入
- 上传

**上传图片示例：**
```java
File file = new File(imagePath);
RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

RetrofitClient.getApiService(this).uploadImage(body).enqueue(new Callback<>() {
    @Override
    public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                           Response<ApiResponse<Map<String, String>>> response) {
        String imageUrl = response.body().getData().get("url");
        // 使用imageUrl创建菜品
    }
    // ...
});
```

## 测试说明

### 测试账号

- 用户名：`testuser1`
- 密码：`123456`

### 测试流程

1. **登录测试**
   - 输入用户名和密码
   - 点击登录
   - 验证跳转到主页

2. **点菜测试**
   - 点击"开始点菜"
   - 选择菜品添加到购物车
   - 点击"推送菜单"
   - 验证推送成功

3. **推送记录测试**
   - 查看"已推送菜单"
   - 验证显示刚才推送的菜品
   - 点击删除（只能删除自己的推送）

4. **二维码测试**
   - 生成自己的二维码
   - 扫描好友二维码
   - 验证添加好友成功

## 常见问题

### Q1: 无法连接到后端

**检查清单：**
1. 后端是否启动（http://localhost:8883/api）
2. 手机和电脑是否在同一WiFi
3. IP地址是否正确
4. 是否添加了网络权限（INTERNET）
5. 是否设置了 `usesCleartextTraffic="true"`

### Q2: 二维码扫描黑屏

**解决方法：**
1. 添加相机权限
2. 动态请求权限（Android 6.0+）

### Q3: 图片选择失败

**解决方法：**
1. 添加存储权限
2. Android 13+使用 `READ_MEDIA_IMAGES` 权限
3. 动态请求权限

## 技术栈

- **语言**：Java
- **最低SDK**：API 33 (Android 13)
- **架构**：MVVM + Repository
- **网络**：Retrofit 2.9.0 + OkHttp 4.12.0
- **图片**：Glide 4.16.0
- **二维码**：ZXing 4.3.0
- **JSON**：Gson 2.10.1

## 开发进度

- [x] 项目结构搭建
- [x] 网络层配置
- [x] 数据模型定义
- [x] API接口定义
- [ ] 登录/注册UI
- [ ] 主页UI
- [ ] 点菜功能
- [ ] 推送记录功能
- [ ] 二维码功能
- [ ] 上传菜品功能
- [ ] WebSocket实时推送

## 参考资料

- [Android官方文档](https://developer.android.com/)
- [Retrofit官方文档](https://square.github.io/retrofit/)
- [Glide官方文档](https://bumptech.github.io/glide/)
- [ZXing Android库](https://github.com/journeyapps/zxing-android-embedded)
- [后端API文档](../springboot-backend/README.md)

---

**开发日期**：2026-01-15
**版本**：v1.0.0
