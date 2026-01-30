# AI美食顾问功能实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标:** 在"今天吃什么"应用中添加AI美食顾问功能，用户可以与AI对话获取菜品推荐、烹饪建议和美食知识。

**架构:** 后端集成智谱AI API提供聊天接口，Android端实现标准聊天UI界面，使用Retrofit进行HTTP通信，支持实时对话交互。

**技术栈:**
- **后端:** Spring Boot 3.2.1, RestTemplate, Zhipu AI API (GLM-4)
- **Android:** Java 17, RecyclerView, Retrofit 2.9.0, Gson
- **AI服务:** 智谱AI开放平台 (https://open.bigmodel.cn/)

---

## 功能设计

### UI设计决策

**主页面新增卡片:**
- **名称:** "AI美食顾问"
- **图标:** 🤖 (机器人emoji)
- **位置:** 主页GridLayout第8个卡片（第4行第2列）
- **样式:** 与现有卡片一致，白色背景，居中布局

**聊天界面设计:**
- **顶部标题栏:** 橙色背景 (#FF6600)，返回按钮 + "AI美食顾问"标题
- **聊天区域:** RecyclerView + 垂直LinearLayoutManager
- **消息气泡:**
  - 用户消息：右侧，橙色背景 (#FF6600)，白色文字
  - AI消息：左侧，浅灰背景 (#F5F5F5)，黑色文字
- **底部输入区:** EditText + 发送按钮，固定在底部

**基础Prompt模板:**
```
你是一位专业的美食顾问，专门帮助用户解决"今天吃什么"的问题。

你的职责：
1. 根据用户喜好推荐菜品
2. 提供简单的烹饪建议
3. 解答美食相关问题

请用简洁、友好的语气回复，每次回复控制在100字以内。
```

---

## 任务分解

### 任务1: 后端 - 添加智谱AI配置和依赖

**文件:**
- 修改: `springboot-backend/pom.xml`
- 修改: `springboot-backend/src/main/resources/application.yml`

**步骤1: 添加HttpClient依赖到pom.xml**

在 `<dependencies>` 标签内添加：

```xml
<!-- HttpClient for AI API (Spring Boot 3.x 内置) -->
<!-- 无需额外依赖，使用 Spring 的 RestTemplate -->
```

**步骤2: 添加智谱AI配置到application.yml**

在文件末尾添加：

```yaml
# 智谱AI配置
zhipuai:
  api-key: ${ZHIPUAI_API_KEY:your-api-key-here}
  api-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  model: glm-4-flash
  base-prompt: |
    你是一位专业的美食顾问，专门帮助用户解决"今天吃什么"的问题。
    你的职责：
    1. 根据用户喜好推荐菜品
    2. 提供简单的烹饪建议
    3. 解答美食相关问题
    请用简洁、友好的语气回复，每次回复控制在100字以内。
```

**步骤3: 验证配置格式**

运行: `cd springboot-backend && "/c/Program Files/JetBrains/IntelliJ IDEA 2025.2.5/plugins/maven/lib/maven3/bin/mvn" compile`
预期输出: BUILD SUCCESS

**步骤4: 提交**

```bash
git add springboot-backend/pom.xml springboot-backend/src/main/resources/application.yml
git commit -m "config: 添加智谱AI配置

- 配置API Key和API URL
- 设置GLM-4-Flash模型
- 添加基础Prompt模板

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务2: 后端 - 创建AI相关DTO类

**文件:**
- 创建: `springboot-backend/src/main/java/com/what2eat/dto/ZhipuAIRequest.java`
- 创建: `springboot-backend/src/main/java/com/what2eat/dto/ZhipuAIResponse.java`
- 创建: `springboot-backend/src/main/java/com/what2eat/dto/ChatMessage.java`

**步骤1: 创建ChatMessage类**

创建文件 `springboot-backend/src/main/java/com/what2eat/dto/ChatMessage.java`:

```java
package com.what2eat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @JsonProperty("role")
    private String role; // system, user, assistant

    @JsonProperty("content")
    private String content;
}
```

**步骤2: 创建ZhipuAIRequest类**

创建文件 `springboot-backend/src/main/java/com/what2eat/dto/ZhipuAIRequest.java`:

```java
package com.what2eat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 智谱AI请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhipuAIRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<ChatMessage> messages;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("max_tokens")
    private Integer maxTokens;
}
```

**步骤3: 创建ZhipuAIResponse类**

创建文件 `springboot-backend/src/main/java/com/what2eat/dto/ZhipuAIResponse.java`:

```java
package com.what2eat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 智谱AI响应DTO
 */
@Data
public class ZhipuAIResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("model")
    private String model;

    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("usage")
    private Usage usage;

    @Data
    public static class Choice {
        @JsonProperty("index")
        private Integer index;

        @JsonProperty("message")
        private ChatMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
```

**步骤4: 编译验证**

运行: `cd springboot-backend && "/c/Program Files/JetBrains/IntelliJ IDEA 2025.2.5/plugins/maven/lib/maven3/bin/mvn" compile`
预期输出: BUILD SUCCESS

**步骤5: 提交**

```bash
git add springboot-backend/src/main/java/com/what2eat/dto/
git commit -m "feat: 添加智谱AI DTO类

- ChatMessage: 聊天消息
- ZhipuAIRequest: AI请求
- ZhipuAIResponse: AI响应

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务3: 后端 - 创建AI服务类和配置类

**文件:**
- 创建: `springboot-backend/src/main/java/com/what2eat/config/ZhipuAIConfig.java`
- 创建: `springboot-backend/src/main/java/com/what2eat/service/ZhipuAIService.java`

**步骤1: 创建ZhipuAIConfig配置类**

创建文件 `springboot-backend/src/main/java/com/what2eat/config/ZhipuAIConfig.java`:

```java
package com.what2eat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱AI配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zhipuai")
public class ZhipuAIConfig {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API URL
     */
    private String apiUrl;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 基础Prompt
     */
    private String basePrompt;
}
```

**步骤2: 创建ZhipuAIService服务类**

创建文件 `springboot-backend/src/main/java/com/what2eat/service/ZhipuAIService.java`:

```java
package com.what2eat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.what2eat.config.ZhipuAIConfig;
import com.what2eat.dto.ChatMessage;
import com.what2eat.dto.ZhipuAIRequest;
import com.what2eat.dto.ZhipuAIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 智谱AI服务
 */
@Service
@Slf4j
public class ZhipuAIService {

    @Autowired
    private ZhipuAIConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送聊天消息到AI
     *
     * @param userMessage 用户消息
     * @return AI回复
     */
    public String chat(String userMessage) {
        try {
            // 构建请求
            ZhipuAIRequest request = buildRequest(userMessage);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            // 发送请求
            HttpEntity<ZhipuAIRequest> entity = new HttpEntity<>(request, headers);
            log.info("发送AI请求: {}", userMessage);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            String responseBody = response.getBody();
            log.info("收到AI响应: {}", responseBody);

            JsonNode root = objectMapper.readTree(responseBody);
            String aiMessage = root.path("choices").get(0).path("message").path("content").asText();

            return aiMessage;

        } catch (Exception e) {
            log.error("AI调用失败", e);
            return "抱歉，我现在无法回答，请稍后再试。";
        }
    }

    /**
     * 构建AI请求
     */
    private ZhipuAIRequest buildRequest(String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统Prompt
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(config.getBasePrompt());
        messages.add(systemMessage);

        // 添加用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        messages.add(userMsg);

        // 构建请求
        ZhipuAIRequest request = new ZhipuAIRequest();
        request.setModel(config.getModel());
        request.setMessages(messages);
        request.setTemperature(0.7);
        request.setTopP(0.9);
        request.setMaxTokens(150);

        return request;
    }
}
```

**步骤3: 在配置类中注册RestTemplate**

修改文件 `springboot-backend/src/main/java/com/what2eat/config/RestTemplateConfig.java`（如果不存在则创建）:

```java
package com.what2eat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**步骤4: 编译验证**

运行: `cd springboot-backend && "/c/Program Files/JetBrains/IntelliJ IDEA 2025.2.5/plugins/maven/lib/maven3/bin/mvn" compile`
预期输出: BUILD SUCCESS

**步骤5: 提交**

```bash
git add springboot-backend/src/main/java/com/what2eat/config/ springboot-backend/src/main/java/com/what2eat/service/ZhipuAIService.java
git commit -m "feat: 实现智谱AI服务

- ZhipuAIConfig: AI配置类
- ZhipuAIService: AI服务，实现聊天功能
- RestTemplateConfig: HTTP客户端配置

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务4: 后端 - 创建AI控制器

**文件:**
- 创建: `springboot-backend/src/main/java/com/what2eat/controller/AIController.java`
- 修改: `springboot-backend/src/main/java/com/what2eat/dto/ChatRequest.java`

**步骤1: 创建ChatRequest DTO**

创建文件 `springboot-backend/src/main/java/com/what2eat/dto/ChatRequest.java`:

```java
package com.what2eat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String message;
}
```

**步骤2: 创建AIController控制器**

创建文件 `springboot-backend/src/main/java/com/what2eat/controller/AIController.java`:

```java
package com.what2eat.controller;

import com.what2eat.dto.ApiResponse;
import com.what2eat.dto.ChatRequest;
import com.what2eat.service.ZhipuAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI聊天控制器
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AIController {

    @Autowired
    private ZhipuAIService zhipuAIService;

    /**
     * AI聊天接口
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(@RequestBody ChatRequest request) {
        try {
            log.info("收到AI聊天请求: {}", request.getMessage());

            // 调用AI服务
            String aiResponse = zhipuAIService.chat(request.getMessage());

            // 构建响应
            Map<String, String> data = new HashMap<>();
            data.put("message", aiResponse);

            return ResponseEntity.ok(ApiResponse.success("成功", data));

        } catch (Exception e) {
            log.error("AI聊天失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "AI服务暂时不可用"));
        }
    }
}
```

**步骤3: 编译验证**

运行: `cd springboot-backend && "/c/Program Files/JetBrains/IntelliJ IDEA 2025.2.5/plugins/maven/lib/maven3/bin/mvn" compile`
预期输出: BUILD SUCCESS

**步骤4: 本地测试（可选）**

启动后端并测试:
```bash
curl -X POST http://localhost:8883/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"今天中午吃什么？"}'
```

预期输出: 包含AI回复的JSON

**步骤5: 提交**

```bash
git add springboot-backend/src/main/java/com/what2eat/controller/AIController.java springboot-backend/src/main/java/com/what2eat/dto/ChatRequest.java
git commit -m "feat: 添加AI聊天接口

- POST /ai/chat: AI聊天接口
- ChatRequest: 聊天请求DTO

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务5: Android - 添加AI相关数据模型

**文件:**
- 创建: `android-app/app/src/main/java/com/what2eat/data/model/ChatMessage.java`
- 创建: `android-app/app/src/main/java/com/what2eat/data/model/ChatResponse.java`
- 修改: `android-app/app/src/main/java/com/what2eat/data/api/ApiService.java`

**步骤1: 创建ChatMessage模型**

创建文件 `android-app/app/src/main/java/com/what2eat/data/model/ChatMessage.java`:

```java
package com.what2eat.data.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息模型
 */
public class ChatMessage implements Serializable {

    private String id;
    private String content;
    private boolean isUser; // true: 用户消息, false: AI消息
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String content, boolean isUser) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.content = content;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
```

**步骤2: 创建ChatResponse模型**

创建文件 `android-app/app/src/main/java/com/what2eat/data/model/ChatResponse.java`:

```java
package com.what2eat.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * AI聊天响应模型
 */
public class ChatResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ChatData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatData getData() {
        return data;
    }

    public void setData(ChatData data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return code == 200;
    }

    public static class ChatData {
        @SerializedName("message")
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
```

**步骤3: 在ApiService中添加聊天接口**

修改文件 `android-app/app/src/main/java/com/what2eat/data/api/ApiService.java`:

在接口末尾（第105行之前）添加：

```java
    // ========== AI聊天接口 ==========

    @POST("ai/chat")
    Call<ChatResponse> sendChatMessage(@Body Map<String, String> request);
```

**步骤4: 编译验证**

运行: `cd android-app && ./gradlew.bat compileDebugJava`
预期输出: BUILD SUCCESSFUL

**步骤5: 提交**

```bash
git add android-app/app/src/main/java/com/what2eat/data/model/ android-app/app/src/main/java/com/what2eat/data/api/ApiService.java
git commit -m "feat: Android添加AI聊天数据模型

- ChatMessage: 聊天消息模型
- ChatResponse: AI响应模型
- ApiService: 添加聊天接口

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务6: Android - 创建聊天Adapter

**文件:**
- 创建: `android-app/app/src/main/java/com/what2eat/ui/ai/ChatAdapter.java`
- 创建: `android-app/app/src/main/res/layout/item_chat_message_user.xml`
- 创建: `android-app/app/src/main/res/layout/item_chat_message_ai.xml`

**步骤1: 创建用户消息布局**

创建文件 `android-app/app/src/main/res/layout/item_chat_message_user.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="end"
    android:padding="8dp">

    <TextView
        android:id="@+id/tvMessage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_chat_user"
        android:text="用户消息"
        android:textSize="16sp"
        android:textColor="#FFFFFF"
        android:padding="12dp"
        android:maxWidth="260dp" />
</LinearLayout>
```

**步骤2: 创建AI消息布局**

创建文件 `android-app/app/src/main/res/layout/item_chat_message_ai.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="start"
    android:padding="8dp">

    <TextView
        android:id="@+id/tvMessage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_chat_ai"
        android:text="AI消息"
        android:textSize="16sp"
        android:textColor="#000000"
        android:padding="12dp"
        android:maxWidth="260dp" />
</LinearLayout>
```

**步骤3: 创建聊天气泡背景drawable**

创建文件 `android-app/app/src/main/res/drawable/bg_chat_user.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FF6600" />
    <corners android:radius="16dp" />
</shape>
```

创建文件 `android-app/app/src/main/res/drawable/bg_chat_ai.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F5F5F5" />
    <corners android:radius="16dp" />
</shape>
```

**步骤4: 创建ChatAdapter**

创建文件 `android-app/app/src/main/java/com/what2eat/ui/ai/ChatAdapter.java`:

```java
package com.what2eat.ui.ai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.what2eat.R;
import com.what2eat.data.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天消息适配器
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<>();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_user, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_ai, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.tvMessage.setText(message.getContent());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
```

**步骤5: 编译验证**

运行: `cd android-app && ./gradlew.bat compileDebugJava`
预期输出: BUILD SUCCESSFUL

**步骤6: 提交**

```bash
git add android-app/app/src/main/java/com/what2eat/ui/ai/ android-app/app/src/main/res/layout/item_chat_*.xml android-app/app/src/main/res/drawable/bg_chat_*.xml
git commit -m "feat: Android添加聊天UI组件

- ChatAdapter: 聊天消息适配器
- item_chat_message_user: 用户消息布局
- item_chat_message_ai: AI消息布局
- bg_chat_user: 用户消息气泡
- bg_chat_ai: AI消息气泡

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务7: Android - 创建聊天Activity

**文件:**
- 创建: `android-app/app/src/main/java/com/what2eat/ui/ai/AIChatActivity.java`
- 创建: `android-app/app/src/main/res/layout/activity_ai_chat.xml`
- 修改: `android-app/app/src/main/AndroidManifest.xml`

**步骤1: 创建聊天界面布局**

创建文件 `android-app/app/src/main/res/layout/activity_ai_chat.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F5F5F5">

    <!-- 顶部标题栏 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:background="#FF6600"
        android:elevation="4dp">

        <ImageButton
            android:id="@+id/btnBack"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@android:drawable/ic_menu_revert"
            android:tint="#FFFFFF"
            android:contentDescription="返回" />

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="AI美食顾问"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="#FFFFFF"
            android:gravity="center" />

        <View
            android:layout_width="48dp"
            android:layout_height="48dp" />
    </LinearLayout>

    <!-- 聊天消息列表 -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewChat"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:padding="8dp"
        android:clipToPadding="false" />

    <!-- 底部输入区域 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp"
        android:background="#FFFFFF"
        android:elevation="8dp">

        <EditText
            android:id="@+id/etMessage"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="输入你的问题..."
            android:minHeight="48dp"
            android:padding="12dp"
            android:background="@drawable/bg_input"
            android:maxLines="4"
            android:gravity="top|start" />

        <Button
            android:id="@+id/btnSend"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="发送"
            android:layout_marginStart="8dp"
            android:background="#FF6600"
            android:textColor="#FFFFFF"
            android:minHeight="48dp" />
    </LinearLayout>

</LinearLayout>
```

**步骤2: 创建输入框背景**

创建文件 `android-app/app/src/main/res/drawable/bg_input.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F5F5F5" />
    <corners android:radius="24dp" />
    <stroke
        android:width="1dp"
        android:color="#DDDDDD" />
</shape>
```

**步骤3: 创建AIChatActivity**

创建文件 `android-app/app/src/main/java/com/what2eat/ui/ai/AIChatActivity.java`:

```java
package com.what2eat.ui.ai;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
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

/**
 * AI聊天Activity
 */
public class AIChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private Button btnSend;
    private Button btnBack;

    private ChatAdapter chatAdapter;
    private ApiService apiService;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        initViews();
        initData();
        setListeners();

        // 添加欢迎消息
        addWelcomeMessage();
    }

    private void initViews() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        // 设置RecyclerView
        chatAdapter = new ChatAdapter(this);
        recyclerViewChat.setAdapter(chatAdapter);

        // 初始化进度对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("AI思考中...");
        progressDialog.setCancelable(false);
    }

    private void initData() {
        apiService = RetrofitClient.getApiService(this);
    }

    private void setListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 发送按钮
        btnSend.setOnClickListener(v -> sendMessage());

        // 回车发送
        etMessage.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    /**
     * 添加欢迎消息
     */
    private void addWelcomeMessage() {
        ChatMessage welcomeMsg = new ChatMessage(
                "你好！我是AI美食顾问，我可以帮你推荐菜品、解答美食问题。请问今天想吃什么？",
                false
        );
        chatAdapter.addMessage(welcomeMsg);
        scrollToBottom();
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String message = etMessage.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        // 添加用户消息
        ChatMessage userMessage = new ChatMessage(message, true);
        chatAdapter.addMessage(userMessage);
        scrollToBottom();

        // 清空输入框
        etMessage.setText("");

        // 发送到AI
        sendToAI(message);
    }

    /**
     * 发送消息到AI
     */
    private void sendToAI(String message) {
        progressDialog.show();

        Map<String, String> request = new HashMap<>();
        request.put("message", message);

        apiService.sendChatMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    if (chatResponse.isSuccess() && chatResponse.getData() != null) {
                        String aiMessage = chatResponse.getData().getMessage();

                        // 添加AI消息
                        ChatMessage aiMsg = new ChatMessage(aiMessage, false);
                        chatAdapter.addMessage(aiMsg);
                        scrollToBottom();
                    } else {
                        showError("AI服务异常");
                    }
                } else {
                    showError("网络错误");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                progressDialog.dismiss();
                showError("连接失败: " + t.getMessage());
            }
        });
    }

    /**
     * 显示错误
     */
    private void showError(String error) {
        ChatMessage errorMsg = new ChatMessage("抱歉，" + error, false);
        chatAdapter.addMessage(errorMsg);
        scrollToBottom();
    }

    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        recyclerViewChat.postDelayed(() -> {
            if (chatAdapter.getItemCount() > 0) {
                recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        }, 100);
    }
}
```

**步骤4: 在AndroidManifest.xml注册Activity**

修改文件 `android-app/app/src/main/AndroidManifest.xml`:

在 `<application>` 标签内，PhotoActivity注册之后添加：

```xml
        <activity
            android:name=".ui.ai.AIChatActivity"
            android:exported="false"
            android:windowSoftInputMode="adjustResize" />
```

**步骤5: 编译验证**

运行: `cd android-app && ./gradlew.bat compileDebugJava`
预期输出: BUILD SUCCESSFUL

**步骤6: 提交**

```bash
git add android-app/app/src/main/java/com/what2eat/ui/ai/AIChatActivity.java android-app/app/src/main/res/layout/activity_ai_chat.xml android-app/app/src/main/res/drawable/bg_input.xml android-app/app/src/main/AndroidManifest.xml
git commit -m "feat: Android添加AI聊天Activity

- AIChatActivity: 聊天界面实现
- activity_ai_chat: 聊天界面布局
- 支持发送消息和接收AI回复

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务8: Android - 在主页添加AI入口

**文件:**
- 修改: `android-app/app/src/main/res/layout/activity_main.xml`
- 修改: `android-app/app/src/main/java/com/what2eat/ui/main/MainActivity.java`
- 修改: `android-app/app/src/main/res/values/strings.xml`

**步骤1: 在strings.xml添加字符串**

修改文件 `android-app/app/src/main/res/values/strings.xml`:

在 `<resources>` 标签内添加：

```xml
    <string name="ai_consultant">AI美食顾问</string>
```

**步骤2: 在主页布局添加AI卡片**

修改文件 `android-app/app/src/main/res/layout/activity_main.xml`:

在 `cardMyPhotos` 的 `</androidx.cardview.widget.CardView>` 之后、`</GridLayout>` 之前添加：

```xml
            <!-- AI美食顾问 -->
            <androidx.cardview.widget.CardView
                android:id="@+id/cardAIConsultant"
                android:layout_width="0dp"
                android:layout_height="120dp"
                android:layout_columnWeight="1"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                app:cardCornerRadius="12dp"
                app:cardElevation="4dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:padding="16dp"
                    android:gravity="center"
                    android:background="@color/white">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="🤖"
                        android:textSize="36sp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/ai_consultant"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"
                        android:layout_marginTop="8dp" />
                </LinearLayout>
            </androidx.cardview.widget.CardView>
```

**步骤3: 修改GridLayout的rowCount**

在同一文件中，将 `rowCount` 从 `5` 改为 `6`:

```xml
            android:rowCount="6"
```

**步骤4: 在MainActivity添加AI卡片逻辑**

修改文件 `android-app/app/src/main/java/com/what2eat/ui/main/MainActivity.java`:

在类的成员变量区域（第48行之后）添加：

```java
    private CardView cardAIConsultant;
```

在 `initViews()` 方法（第99行之后）添加：

```java
        cardAIConsultant = findViewById(R.id.cardAIConsultant);
```

在 `setListeners()` 方法（第135行之后）添加：

```java
        // AI美食顾问
        cardAIConsultant.setOnClickListener(v -> {
            startActivity(new Intent(this, com.what2eat.ui.ai.AIChatActivity.class));
        });
```

**步骤5: 编译验证**

运行: `cd android-app && ./gradlew.bat assembleDebug`
预期输出: BUILD SUCCESSFUL

**步骤6: 提交**

```bash
git add android-app/app/src/main/res/layout/activity_main.xml android-app/app/src/main/res/values/strings.xml android-app/app/src/main/java/com/what2eat/ui/main/MainActivity.java
git commit -m "feat: 在主页添加AI美食顾问入口

- activity_main: 添加AI卡片（第8个卡片）
- MainActivity: 添加AI卡片点击事件
- strings: 添加ai_consultant字符串

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### 任务9: 集成测试和部署

**文件:**
- 部署: `springboot-backend/target/what2eat-backend-1.0.0.jar`
- 部署: `android-app/app/build/outputs/apk/debug/app-debug.apk`

**步骤1: 编译后端**

运行: `cd springboot-backend && "/c/Program Files/JetBrains/IntelliJ IDEA 2025.2.5/plugins/maven/lib/maven3/bin/mvn" clean package -DskipTests`
预期输出: BUILD SUCCESS

**步骤2: 部署后端到服务器**

运行:
```bash
scp springboot-backend/target/what2eat-backend-1.0.0.jar root@47.83.126.42:/root/what2eat/target/
ssh root@47.83.126.42 "cd /root/what2eat && docker compose restart app"
sleep 8
```
预期输出: Container started

**步骤3: 测试后端API**

运行:
```bash
curl -s http://api.jamesweb.org:8883/api/ai/chat \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"message":"今天中午吃什么？"}'
```
预期输出: 包含AI回复的JSON响应

**步骤4: 编译Android APK**

运行: `cd android-app && ./gradlew.bat clean assembleDebug`
预期输出: BUILD SUCCESSFUL

**步骤5: 安装APK到设备**

运行: `adb install android-app/app/build/outputs/apk/debug/app-debug.apk`
预期输出: Success

**步骤6: 功能测试**

在设备上测试:
1. 打开"今天吃什么"APP
2. 登录
3. 主页找到"AI美食顾问"卡片（🤖 图标）
4. 点击进入聊天界面
5. 输入"今天中午吃什么？"
6. 点击发送
7. 验证AI回复内容

**预期结果:**
- 聊天界面正常显示
- 消息气泡正确显示（用户右侧橙色，AI左侧灰色）
- AI能够理解并回复美食相关问题
- 回复内容简洁、友好

**步骤7: 提交完整功能**

```bash
git add .
git commit -m "feat: 完成AI美食顾问功能

- 后端集成智谱AI API
- Android实现聊天界面
- 主页添加AI入口卡片
- 支持实时对话交互

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## 测试检查清单

### 后端测试
- [ ] AI接口返回正确的JSON格式
- [ ] API Key正确配置
- [ ] Prompt模板正确加载
- [ ] 错误处理正常工作

### Android测试
- [ ] 主页显示AI卡片
- [ ] 点击卡片进入聊天界面
- [ ] 聊天界面UI正常
- [ ] 发送消息后显示用户气泡
- [ ] 接收到AI回复并显示气泡
- [ ] 支持多轮对话
- [ ] 返回按钮正常工作
- [ ] 输入框支持回车发送

### UI/UX测试
- [ ] 消息气泡样式正确
- [ ] 自动滚动到最新消息
- [ ] 加载进度显示正常
- [ ] 网络错误有提示

---

## 后续优化（Phase 2）

1. **Prompt优化训练** - 单独制定训练方案
2. **多轮对话上下文** - 保存对话历史
3. **流式响应** - 实现打字机效果
4. **图片生成** - 集成菜品图片生成功能
5. **快捷问题** - 添加预设问题按钮
6. **语音输入** - 支持语音转文字

---

## 参考文档

- **智谱AI开放平台**: https://open.bigmodel.cn/
- **GLM-4 API文档**: https://open.bigmodel.cn/dev/api
- **Android RecyclerView**: https://developer.android.com/guide/topics/ui/layout/recyclerview
