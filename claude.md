# Claude Code 项目配置文件

## Maven 路径

Maven 执行路径:
```
C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd
```

## 常用命令

### 编译项目
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" compile
```

### 打包项目
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" package -DskipTests
```

### 启动后端服务
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

### 完整构建（测试+打包）
```bash
cd springboot-backend
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean install
```

## 项目信息

- **后端端口**: 8883
- **API 地址**: http://10.88.1.127:8883/api/
- **WebSocket**: ws://10.88.1.127:8883/api/ws
- **Swagger UI**: http://10.88.1.127:8883/api/swagger-ui.html

## 数据库

- **数据库**: what2eat
- **端口**: 3306
- **初始化脚本**: init_database.sql
