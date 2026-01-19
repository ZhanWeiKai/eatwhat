@echo off
echo ====================================
echo 今天吃什么 - 后端启动脚本
echo ====================================
echo.

REM 设置项目目录
set PROJECT_DIR=C:\claude-project\eatwhat\springboot-backend
cd /d "%PROJECT_DIR%"

echo 当前目录: %CD%
echo.

REM 检查Java环境
echo 检查Java环境...
java -version 2>nul
if errorlevel 1 (
    echo [错误] 未找到Java，请先安装Java 17或更高版本
    pause
    exit /b 1
)
echo.

REM 方法1：查找Maven的多种可能位置
set MAVEN_CMD=

REM 1a. 检查PATH中的mvn
where mvn.cmd >nul 2>&1
if not errorlevel 1 (
    set MAVEN_CMD=mvn.cmd
    goto :found_maven
)

REM 1b. 检查项目根目录的mvnw
if exist mvnw.cmd (
    set MAVEN_CMD=mvnw.cmd
    goto :found_maven
)

REM 1c. 检查常见的Maven安装路径
if exist "C:\Program Files\Apache\Maven\bin\mvn.cmd" (
    set "MAVEN_CMD=C:\Program Files\Apache\Maven\bin\mvn.cmd"
    goto :found_maven
)

if exist "C:\Maven\bin\mvn.cmd" (
    set "MAVEN_CMD=C:\Maven\bin\mvn.cmd"
    goto :found_maven
)

REM 1d. 检查IDEA自带的Maven（常见路径）
if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.2\plugins\maven\lib\maven3\bin\mvn.cmd" (
    set "MAVEN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.2\plugins\maven\lib\maven3\bin\mvn.cmd"
    goto :found_maven
)

if exist "C:\Users\%USERNAME%\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\*\plugins\maven\lib\maven3\bin\mvn.cmd" (
    for /f "delims=" %%i in ('dir /b /s "C:\Users\%USERNAME%\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\*\plugins\maven\lib\maven3\bin\mvn.cmd" 2^>nul ^| findstr "mvn.cmd"') do (
        set "MAVEN_CMD=%%i"
        goto :found_maven
    )
)

REM 未找到Maven
echo [错误] 未找到Maven！
echo.
echo 请选择以下方式之一启动后端：
echo.
echo 1. 使用IDEA启动（推荐）:
echo    - 打开IDEA
echo    - 打开项目: %PROJECT_DIR%
echo    - 运行 src/main/java/com/what2eat/What2EatApplication.java
echo.
echo 2. 安装Maven并添加到PATH
echo.
echo 3. 查看《IDEA启动指南.md》获取详细说明
echo.
pause
exit /b 1

:found_maven
echo [成功] 找到Maven: %MAVEN_CMD%
echo.

echo 启动Spring Boot后端...
echo 地址: http://10.88.1.127:8883/api/
echo.
echo 按 Ctrl+C 停止服务器
echo ====================================
echo.

call "%MAVEN_CMD%" spring-boot:run

echo.
echo 后端已停止
pause
