package com.what2eat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 今天吃什么 - 应用启动类
 *
 * @author What2Eat Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class What2EatApplication {

    public static void main(String[] args) {
        SpringApplication.run(What2EatApplication.class, args);
        System.out.println("""

                ========================================
                   今天吃什么 - 后端服务启动成功！
                   API文档: http://localhost:8883/api/swagger-ui.html
                   WebSocket: ws://localhost:8883/api/ws
                ========================================
                """);
    }

}
