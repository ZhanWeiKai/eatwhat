# IDEA启动后端指南

## 方法1：使用IDEA直接运行（推荐）

### 步骤1：打开项目
1. 启动IntelliJ IDEA
2. 点击 `File` → `Open`
3. 选择 `C:\claude-project\eatwhat\springboot-backend` 目录
4. 等待Maven依赖下载完成（右下角会显示进度）

### 步骤2：配置运行
1. 找到文件：`src/main/java/com/what2eat/What2EatApplication.java`
2. 右键点击文件
3. 选择 `Run 'What2EatApplication'` （或按 `Shift + F10`）

### 步骤3：验证启动成功
看到以下日志表示启动成功：
```
  ____                    _           _ ____
 / ___| _   _  ___  ___| |__   ___ |  _ \
 \___ \| | | |/ _ \/ __| '_ \ / _ \| | | |
  ___) | |_| | (_) \__ \ | | | (_) | |_| | |
 |____/ \__, |\___/___/_| |_|\___/|____/_|
         |___/

Started What2EatApplication in X.XXX seconds
Tomcat started on port(s): 8883 (http) with context path '/api'
```

---

## 方法2：使用IDEA的Maven面板

### 步骤1：打开Maven面板
1. 点击IDEA右侧的 `Maven` 标签
2. 展开 `what2eat-backend` 项目

### 步骤2：运行Spring Boot
1. 展开 `Plugins` → `spring-boot`
2. 双击 `spring-boot:run`

---

## 方法3：配置运行配置（推荐用于快速启动）

### 步骤1：创建运行配置
1. 点击顶部菜单 `Run` → `Edit Configurations...`
2. 点击左上角 `+` 按钮
3. 选择 `Spring Boot`

### 步骤2：配置参数
- **Name**: `What2EatApplication`
- **Main class**: `com.what2eat.What2EatApplication`
- **Working directory**: `$ProjectFileDir$`
- **Use classpath of module**: `what2eat-backend`
- **VM options**: `-Dfile.encoding=UTF-8`

### 步骤3：保存并运行
1. 点击 `OK`
2. 点击顶部工具栏的绿色运行按钮（或按 `Shift + F10`）

---

## 验证后端启动

### 1. 检查控制台输出
在IDEA的Run窗口应该看到：
```
Tomcat started on port(s): 8883 (http) with context path '/api'
Started What2EatApplication in X.XXX seconds
```

### 2. 浏览器访问
打开浏览器访问：http://10.88.1.127:8883/api/

### 3. API文档
访问Swagger文档：http://10.88.1.127:8883/api/swagger-ui.html

---

## 常见问题

### 问题1：Maven依赖下载失败
**解决方法**：
1. 点击 `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven`
2. 检查 `Maven home path` 是否正确（IDEA bundled Maven）
3. 点击 `Apply` 和 `OK`
4. 在Maven面板点击刷新按钮

### 问题2：Java版本不匹配
**解决方法**：
1. 确认安装了Java 17
2. `File` → `Project Structure` → `Project`
3. 设置 `Project SDK` 为 Java 17
4. 设置 `Project language level` 为 17

### 问题3：端口8883被占用
**解决方法**：
```bash
# 查找占用8883端口的进程
netstat -ano | findstr "8883"

# 结束进程
taskkill /F /PID <进程ID>
```

---

## 快速启动（后续使用）

配置完成后，以后只需要：
1. 打开IDEA
2. 按 `Shift + F10`（或点击绿色运行按钮）
3. 等待启动完成

**预计启动时间**：10-20秒
