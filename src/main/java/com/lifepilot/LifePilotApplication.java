package com.lifepilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LifePilot 应用程序启动入口。
 */
@SpringBootApplication
public class LifePilotApplication {

    /**
     * 启动 Spring Boot 应用程序。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(LifePilotApplication.class, args);
    }
}
