# LifePilot MVP

LifePilot 是一个基于 Spring Boot 的个人生活管理 MVP，用于管理待办事项、笔记，并通过 AI Agent 辅助完成日常规划。

## 环境要求

- Java 21
- Gradle，或直接使用项目自带的 Gradle Wrapper
- Docker Desktop，并确保 Linux engine 正在运行
- DeepSeek API Key，用于真实 AI 对话调用

## 本地配置

项目默认启用 `local` profile。本地开发环境默认关闭 API 认证：

```yaml
lifepilot:
  security:
    auth-enabled: false
```

启动应用前，先设置 DeepSeek API Key：

```powershell
$env:DEEPSEEK_V4 = "your-deepseek-api-key"
```

本地 profile 通过 Spring AI 的 OpenAI 兼容客户端访问 DeepSeek：

```yaml
spring:
  ai:
    openai:
      api-key: ${DEEPSEEK_V4:${OPENAI_API_KEY:}}
      base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
      chat:
        options:
          model: ${DEEPSEEK_MODEL:deepseek-chat}
```

如果需要切换模型或接口地址，可以覆盖下面的环境变量：

```powershell
$env:DEEPSEEK_MODEL = "deepseek-chat"
$env:DEEPSEEK_BASE_URL = "https://api.deepseek.com"
```

如果需要在本地测试 JWT 认证，请提供至少 32 字节的密钥，并在配置中开启认证：

```powershell
$env:LIFEPILOT_JWT_SECRET = "change-me-change-me-change-me-change-me"
```

## 启动依赖

启动带 pgvector 的 Postgres 和 Redis：

```powershell
docker compose up -d postgres redis
```

查看容器状态：

```powershell
docker compose ps
```

## 启动应用

运行 Spring Boot 应用：

```powershell
.\gradlew.bat bootRun
```

应用默认访问地址：

```text
http://localhost:8080
```

健康检查：

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## 测试命令

运行自动化测试：

```powershell
.\gradlew.bat test
```

生成 Javadoc：

```powershell
.\gradlew.bat javadoc
```

应用启动后运行本地烟测脚本：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

如果需要指定其他服务地址：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1 -BaseUrl "http://localhost:8080"
```

## MVP 接口

- `GET /actuator/health`
- `POST /api/todos`
- `GET /api/todos`
- `POST /api/notes`
- `GET /api/notes`
- `GET /api/notes/{id}`
- `POST /api/chat`

## 常见问题

如果数据库相关测试连接失败，请确认 Docker Desktop 已启动，并且 Linux engine 可用：

```powershell
docker compose up -d postgres redis
```

如果 PowerShell 因执行策略拒绝运行烟测脚本，请使用上面的 `-ExecutionPolicy Bypass` 命令。该设置只对当前 PowerShell 进程生效，不会修改系统策略。
