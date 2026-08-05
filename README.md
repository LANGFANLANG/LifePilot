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
http://localhost:8081
```

健康检查：

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
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
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1 -BaseUrl "http://localhost:8081"
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

## 当前核心闭环

LifePilot 当前已经从基础 MVP 扩展为一个个人执行闭环：

```text
AI 对话捕获目标 -> 生成计划草案 -> 用户确认 -> 创建待办 -> 今日计划排序 -> 到点提醒 -> 每日复盘
```

### AI 计划草案

当用户在对话里提出多步骤目标时，AI 不直接批量创建待办，而是先创建计划草案。草案保存到：

- `plan_previews`
- `plan_preview_tasks`

前端 Chat 页面会展示计划确认卡片。用户确认后，后端才把草案任务批量转换为真实待办，来源标记为 `ai-plan`。

### 今日工作台

首页 `/` 是 Today Dashboard，包含：

- 今日重点：最多 3 个高价值任务
- 时间线：按 `plannedStartAt` 排序
- Inbox：未规划的待办
- 进度统计：待处理、逾期、预计投入
- 最近提醒：展示站内提醒投递记录

今日排序由 `PlanningService` 完成，规则是确定性的，不依赖模型临场判断。

### 站内提醒

待办设置 `reminderAt` 后，后台定时任务默认每 60 秒扫描一次到期提醒。到期后写入：

- `reminder_deliveries`

同一个 `todo_id + reminder_at + channel` 只会投递一次，避免重复提醒。

### 每日复盘

复盘页面 `/review` 支持按日期生成草稿、编辑和保存。草稿来源包括：

- 当天完成的任务
- 当天未完成的计划任务
- 当天新增任务

AI 复盘接口已预留，当前默认实现会安全降级到确定性草稿。

## 主要页面

- `/`：今日工作台
- `/todos`：待办列表
- `/notes`：笔记
- `/review`：每日复盘
- `/chat`：AI 助手
- `/login`：登录

## 扩展接口

### 计划草案

- `POST /api/plan-previews`
- `GET /api/plan-previews/{id}`
- `POST /api/plan-previews/{id}/confirm`
- `POST /api/plan-previews/{id}/reject`

### 今日计划

- `GET /api/planning/today`

### 提醒

- `GET /api/reminders/recent`

### 每日复盘

- `POST /api/reviews/daily/{date}/draft`
- `GET /api/reviews/daily/{date}`
- `PUT /api/reviews/daily/{date}`

## 数据库迁移

当前 Flyway 迁移文件：

- `V1__init_core_tables.sql`：会话、消息、待办、笔记、执行日志
- `V2__extend_todo_execution_fields.sql`：待办执行字段
- `V3__create_user_accounts.sql`：用户账号
- `V4__create_plan_previews.sql`：AI 计划草案
- `V5__create_reminder_deliveries.sql`：站内提醒投递记录
- `V6__create_daily_reviews.sql`：每日复盘

应用连接数据库启动时，Flyway 会自动执行尚未应用的迁移。
