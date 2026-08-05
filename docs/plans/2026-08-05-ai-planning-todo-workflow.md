# AI 对话规划待办实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标：** 让用户可以在 AI 对话里描述一个目标，先看到 AI 拆出来的计划草案，确认后再生成待办，并能在今日计划页看到排序后的任务、收到提醒、生成每日复盘。

**架构：** 保持当前 Spring Boot 分层结构：Controller 只负责接口和 DTO，Service 负责业务规则，Repository 负责持久化，AI Tool 保持薄封装。AI 负责理解、拆解和解释计划，但真正的校验、排序、确认、落库由确定性的后端服务完成。凡是批量创建、删除、修改待办的行为，都必须经过用户显式确认。

**技术栈：** Java 21、Spring Boot 3.3、Spring AI、Spring Data JPA、Flyway、PostgreSQL/pgvector、Redis、JUnit 5、AssertJ、Mockito、MockMvc、Vue 3、Vite。

---

## 范围

这份计划解决的是 LifePilot 下一阶段的核心体验：通过 AI 对话做计划和待办。

包含：

- 检查并修复中文乱码和前端文案问题。
- 增加 AI 计划草案和确认机制。
- 增加确定性的今日计划排序。
- 改造 Chat UI，让用户能确认 AI 计划。
- 改造 Today Dashboard，让用户每天能看到重点任务、时间线和进度。
- 增加提醒扫描和提醒记录。
- 增加每日复盘草稿和编辑保存。

暂不包含：

- 多用户后台管理。
- 付费和订阅。
- 外部日历同步。
- 邮件、飞书、微信等外部提醒渠道。
- 完整 RAG 知识库。

---

## 当前状态

已经具备：

- 对话接口：`src/main/java/com/lifepilot/controller/ChatController.java`
- Agent 编排：`src/main/java/com/lifepilot/agent/AgentService.java`
- Spring AI 客户端：`src/main/java/com/lifepilot/agent/SpringAiClient.java`
- Todo 工具：`src/main/java/com/lifepilot/tool/TodoTool.java`
- Todo 元数据：`priority`、`category`、`estimatedMinutes`、`plannedStartAt`、`reminderAt`、`completedAt`、`parentTodoId`、`source`、`postponementCount`
- Todo CRUD 接口：`src/main/java/com/lifepilot/controller/TodoController.java`
- Vue 对话页：`frontend/src/views/ChatView.vue`
- Vue 待办页：`frontend/src/views/TodosView.vue`

主要缺口：

- AI 现在可以直接创建待办，但没有“计划预览 -> 用户确认 -> 批量落库”的安全流程。
- Todo 列表目前按创建时间排序，不是按执行价值排序。
- 后端已有计划开始和提醒字段，但 Vue 表单还没有完整暴露。
- 提醒字段存在，但没有真正的提醒扫描和提醒记录。
- 每日复盘还没有实现。
- 部分中文文案在当前读取结果里出现乱码，需要确认浏览器内是否也乱码。

---

## 阶段一：先把当前体验修顺

### 任务 1：检查并修复中文文案和编码

**文件：**
- 检查：`README.md`
- 检查：`docs/01_产品需求.md`
- 检查：`docs/plans/2026-07-15-personal-execution-assistant-design.md`
- 检查：`frontend/src/views/ChatView.vue`
- 检查：`frontend/src/views/TodosView.vue`
- 检查：`frontend/src/components/TodoForm.vue`
- 检查：`frontend/src/components/TodoItem.vue`
- 检查：`frontend/src/router/index.js`
- 测试：`src/test/java/com/lifepilot/config/StaticPageTest.java`

**执行步骤：**

1. 启动依赖和应用：

```powershell
docker compose up -d postgres redis
.\gradlew.bat bootRun
```

2. 打开 `http://localhost:8081`，检查登录页、待办页、笔记页、对话页的中文是否正常。

3. 如果浏览器里也乱码，修复前端文案。建议统一成：

- 对话空状态：`我可以帮你把想法落成计划`
- 对话输入框：`说点什么，按 Enter 发送`
- 待办筛选：`全部`、`进行中`、`已完成`
- 表单字段：`标题`、`描述`、`优先级`、`分类`、`截止时间`、`预计耗时（分钟）`、`计划开始`、`提醒时间`

4. 在 `frontend/src/components/TodoForm.vue` 里补上 `plannedStartAt` 和 `reminderAt` 两个字段，创建和编辑时都要能保存。

5. 运行验证：

```powershell
.\gradlew.bat test --tests com.lifepilot.config.StaticPageTest
```

---

## 阶段二：AI 只生成草案，用户确认后才落库

### 任务 2：增加 AI 计划草案领域模型

**文件：**
- 新建：`src/main/java/com/lifepilot/domain/PlanPreview.java`
- 新建：`src/main/java/com/lifepilot/domain/PlanPreviewTask.java`
- 新建：`src/main/java/com/lifepilot/domain/PlanPreviewStatus.java`
- 新建：`src/main/java/com/lifepilot/repository/PlanPreviewRepository.java`
- 新建：`src/main/resources/db/migration/V4__create_plan_previews.sql`
- 测试：`src/test/java/com/lifepilot/domain/PlanPreviewTest.java`
- 测试：`src/test/java/com/lifepilot/repository/PlanPreviewRepositoryTest.java`

**要实现的行为：**

- 新建计划草案默认为 `PENDING`。
- 一个计划草案包含多个草案任务。
- 草案任务字段与 Todo 创建字段基本一致。
- 已确认的草案不能重复确认。
- 已拒绝的草案不能再确认。

**数据库迁移：**

```sql
CREATE TABLE plan_previews (
    id UUID PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id),
    goal TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE plan_preview_tasks (
    id UUID PRIMARY KEY,
    plan_preview_id UUID NOT NULL REFERENCES plan_previews(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_at TIMESTAMPTZ,
    priority VARCHAR(20) NOT NULL,
    category VARCHAR(80),
    estimated_minutes INTEGER,
    planned_start_at TIMESTAMPTZ,
    reminder_at TIMESTAMPTZ,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE plan_preview_tasks
    ADD CONSTRAINT plan_preview_tasks_estimated_minutes_non_negative
        CHECK (estimated_minutes IS NULL OR estimated_minutes >= 0);
```

**验证命令：**

```powershell
.\gradlew.bat test --tests "*PlanPreview*"
```

### 任务 3：增加计划草案服务和确认流程

**文件：**
- 新建：`src/main/java/com/lifepilot/service/PlanPreviewService.java`
- 新建：`src/main/java/com/lifepilot/service/dto/CreatePlanPreviewCommand.java`
- 新建：`src/main/java/com/lifepilot/service/dto/PlanPreviewView.java`
- 新建：`src/main/java/com/lifepilot/service/dto/PlanPreviewTaskView.java`
- 修改：`src/main/java/com/lifepilot/service/TodoService.java`
- 测试：`src/test/java/com/lifepilot/service/PlanPreviewServiceTest.java`

**要实现的行为：**

- 创建草案时不创建真实待办。
- 用户确认草案后，批量创建真实待办。
- 确认生成的待办 `source` 写成 `ai-plan`。
- 用户拒绝草案后，不创建待办。
- 不存在的草案、非 `PENDING` 草案不能确认。

**服务方法：**

- `PlanPreviewView create(CreatePlanPreviewCommand command)`
- `PlanPreviewView get(UUID id)`
- `List<TodoView> confirm(UUID id)`
- `PlanPreviewView reject(UUID id)`

**验证命令：**

```powershell
.\gradlew.bat test --tests com.lifepilot.service.PlanPreviewServiceTest
```

### 任务 4：增加计划草案 HTTP 接口

**文件：**
- 新建：`src/main/java/com/lifepilot/controller/PlanPreviewController.java`
- 新建：`src/main/java/com/lifepilot/controller/dto/CreatePlanPreviewRequest.java`
- 新建：`src/main/java/com/lifepilot/controller/dto/PlanPreviewTaskRequest.java`
- 测试：`src/test/java/com/lifepilot/controller/PlanPreviewControllerTest.java`

**接口：**

| 方法 | 路径 | 行为 |
|---|---|---|
| `POST` | `/api/plan-previews` | 创建计划草案 |
| `GET` | `/api/plan-previews/{id}` | 查看计划草案 |
| `POST` | `/api/plan-previews/{id}/confirm` | 确认并创建待办 |
| `POST` | `/api/plan-previews/{id}/reject` | 拒绝计划草案 |

**验证命令：**

```powershell
.\gradlew.bat test --tests com.lifepilot.controller.PlanPreviewControllerTest
```

### 任务 5：增加 AI 规划提示词和结构化响应

**文件：**
- 新建：`src/main/java/com/lifepilot/agent/dto/AgentAction.java`
- 修改：`src/main/java/com/lifepilot/agent/dto/AgentResponse.java`
- 修改：`src/main/java/com/lifepilot/agent/SpringAiClient.java`
- 修改：`src/main/java/com/lifepilot/agent/AiClient.java`
- 修改：`src/main/java/com/lifepilot/agent/AgentService.java`
- 测试：`src/test/java/com/lifepilot/agent/AgentServiceTest.java`
- 测试：`src/test/java/com/lifepilot/agent/SpringAiClientTest.java`

**响应结构：**

```java
public record AgentResponse(
        UUID conversationId,
        String content,
        List<AgentAction> actions
) {
}
```

```java
public record AgentAction(
        String type,
        UUID resourceId,
        String label
) {
}
```

**系统提示词要表达的规则：**

```text
你是 LifePilot，一个个人执行规划助手。
当用户给出目标时，如果缺少截止时间、范围、优先级或可用时间，请先追问。
当目标需要拆成多个任务时，先创建计划草案，等待用户确认。
未经用户明确确认，不要批量创建、删除或修改待办。
调用工具时使用 ISO-8601 日期时间。
任务标题要具体、简短，预计耗时要现实。
```

**验证命令：**

```powershell
.\gradlew.bat test --tests com.lifepilot.agent.*Test
```

### 任务 6：增加计划草案 AI 工具

**文件：**
- 新建：`src/main/java/com/lifepilot/tool/PlanPreviewTool.java`
- 修改：`src/main/java/com/lifepilot/config/AiToolConfig.java`
- 测试：`src/test/java/com/lifepilot/tool/PlanPreviewToolTest.java`
- 测试：`src/test/java/com/lifepilot/agent/SpringAiClientTest.java`

**工具方法：**

- `createPlanPreview(String goal, List<PlanPreviewTaskInput> tasks)`
- `getPlanPreview(UUID id)`

第一版不要把 `confirm` 暴露给 AI 工具。确认必须来自前端按钮或明确接口调用。

**验证命令：**

```powershell
.\gradlew.bat test --tests "*PlanPreviewToolTest" --tests com.lifepilot.agent.SpringAiClientTest
```

### 任务 7：改造 Chat UI，支持确认 AI 计划

**文件：**
- 修改：`frontend/src/api/chat.js`
- 新建：`frontend/src/api/planPreviews.js`
- 修改：`frontend/src/views/ChatView.vue`
- 修改：`frontend/src/styles/main.css`

**前端 API：**

```js
import { request } from './http'

export function getPlanPreview(id) {
  return request(`/api/plan-previews/${id}`)
}

export function confirmPlanPreview(id) {
  return request(`/api/plan-previews/${id}/confirm`, { method: 'POST' })
}

export function rejectPlanPreview(id) {
  return request(`/api/plan-previews/${id}/reject`, { method: 'POST' })
}
```

**计划卡片展示内容：**

- 目标
- 任务数量
- 任务标题
- 优先级
- 计划时间
- 预计耗时
- 确认按钮
- 拒绝按钮

**验证命令：**

```powershell
cd frontend
npm run build
```

---

## 阶段三：今日计划页，让计划真的每天可用

### 任务 8：增加确定性的今日计划服务

**文件：**
- 新建：`src/main/java/com/lifepilot/service/PlanningService.java`
- 新建：`src/main/java/com/lifepilot/service/dto/TodayPlanView.java`
- 新建：`src/main/java/com/lifepilot/service/dto/TodayPlanItemView.java`
- 修改：`src/main/java/com/lifepilot/repository/TodoRepository.java`
- 测试：`src/test/java/com/lifepilot/service/PlanningServiceTest.java`

**排序规则：**

1. 已完成待办不进入今日计划。
2. 已逾期任务排在未逾期任务前。
3. 高优先级排在中、低优先级前。
4. `plannedStartAt` 更早的任务排在更晚任务前。
5. `dueAt` 更早的任务排在更晚任务前。
6. 等待时间更久的任务获得小幅加权。
7. 今日重点最多 3 个任务。

**返回结构：**

- `focus`：排名前三的重点任务
- `timeline`：有 `plannedStartAt` 的任务，按时间排序
- `inbox`：没有 `plannedStartAt` 且没有 `dueAt` 的待处理任务
- `overdueCount`
- `pendingCount`
- `estimatedMinutes`

**验证命令：**

```powershell
.\gradlew.bat test --tests com.lifepilot.service.PlanningServiceTest
```

### 任务 9：增加 Today Dashboard

**文件：**
- 新建：`src/main/java/com/lifepilot/controller/PlanningController.java`
- 测试：`src/test/java/com/lifepilot/controller/PlanningControllerTest.java`
- 新建：`frontend/src/api/planning.js`
- 新建：`frontend/src/views/TodayView.vue`
- 修改：`frontend/src/router/index.js`
- 修改：`frontend/src/App.vue`
- 修改：`frontend/src/styles/main.css`

**接口：**

| 方法 | 路径 | 行为 |
|---|---|---|
| `GET` | `/api/planning/today` | 返回 `TodayPlanView` |

**页面内容：**

- 今日重点
- 时间线
- Inbox
- 进度统计
- AI 建议入口，跳转到 `/chat`

把 `/` 改成 `TodayView`，原始待办列表移动到 `/todos`。

**验证命令：**

```powershell
.\gradlew.bat test --tests com.lifepilot.controller.PlanningControllerTest
cd frontend
npm run build
```

---

## 阶段四：提醒闭环

### 任务 10：增加提醒投递记录

**文件：**
- 新建：`src/main/java/com/lifepilot/domain/ReminderDelivery.java`
- 新建：`src/main/java/com/lifepilot/repository/ReminderDeliveryRepository.java`
- 新建：`src/main/java/com/lifepilot/service/ReminderService.java`
- 新建：`src/main/java/com/lifepilot/service/dto/ReminderDeliveryView.java`
- 新建：`src/main/resources/db/migration/V5__create_reminder_deliveries.sql`
- 测试：`src/test/java/com/lifepilot/service/ReminderServiceTest.java`
- 测试：`src/test/java/com/lifepilot/repository/ReminderDeliveryRepositoryTest.java`

**要实现的行为：**

- 找到 `reminderAt` 已到期的待办。
- 每条到期提醒生成一条投递记录。
- 同一个待办、同一个提醒时间、同一个渠道不能重复投递。
- AI 服务失败不能影响提醒扫描。

**数据库迁移：**

```sql
CREATE TABLE reminder_deliveries (
    id UUID PRIMARY KEY,
    todo_id UUID NOT NULL REFERENCES todos(id) ON DELETE CASCADE,
    reminder_at TIMESTAMPTZ NOT NULL,
    channel VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    message TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX reminder_deliveries_unique_todo_time_channel
    ON reminder_deliveries(todo_id, reminder_at, channel);
```

**服务方法：**

- `List<ReminderDeliveryView> deliverDueReminders(OffsetDateTime now)`
- `List<ReminderDeliveryView> listRecent()`

第一版渠道只做 `IN_APP`。

### 任务 11：增加提醒定时扫描和前端展示

**文件：**
- 新建：`src/main/java/com/lifepilot/config/SchedulerConfig.java`
- 新建：`src/main/java/com/lifepilot/controller/ReminderController.java`
- 测试：`src/test/java/com/lifepilot/controller/ReminderControllerTest.java`
- 新建：`frontend/src/api/reminders.js`
- 修改：`frontend/src/views/TodayView.vue`
- 修改：`frontend/src/styles/main.css`

**实现要点：**

- 在 `SchedulerConfig` 增加 `@EnableScheduling`。
- 每分钟扫描一次到期提醒：

```java
@Scheduled(fixedDelayString = "${lifepilot.reminders.scan-delay-ms:60000}")
public void scan() {
    reminderService.deliverDueReminders(OffsetDateTime.now(clock));
}
```

- 增加接口：`GET /api/reminders/recent`
- 在 Today Dashboard 用通知条或侧栏面板展示最近提醒。

**验证命令：**

```powershell
.\gradlew.bat test --tests "*Reminder*"
```

---

## 阶段五：复盘闭环

### 任务 12：增加每日复盘模型和服务

**文件：**
- 新建：`src/main/java/com/lifepilot/domain/DailyReview.java`
- 新建：`src/main/java/com/lifepilot/repository/DailyReviewRepository.java`
- 新建：`src/main/java/com/lifepilot/service/DailyReviewService.java`
- 新建：`src/main/java/com/lifepilot/service/dto/DailyReviewView.java`
- 新建：`src/main/resources/db/migration/V6__create_daily_reviews.sql`
- 测试：`src/test/java/com/lifepilot/service/DailyReviewServiceTest.java`

**要实现的行为：**

- 草稿包含当天完成的待办。
- 草稿包含当天未完成的计划任务。
- 草稿包含当天新增的任务。
- 用户可以编辑并保存最终复盘。

**数据库迁移：**

```sql
CREATE TABLE daily_reviews (
    id UUID PRIMARY KEY,
    review_date DATE NOT NULL UNIQUE,
    completed_summary TEXT,
    unfinished_summary TEXT,
    new_tasks_summary TEXT,
    reflection TEXT,
    tomorrow_plan TEXT,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

**服务方法：**

- `DailyReviewView draft(LocalDate date)`
- `DailyReviewView save(LocalDate date, SaveDailyReviewCommand command)`
- `DailyReviewView get(LocalDate date)`

第一版可以先用确定性逻辑生成草稿，不依赖 AI。确定性测试通过后，再接 AI 润色。

### 任务 13：增加每日复盘 API 和前端页面

**文件：**
- 新建：`src/main/java/com/lifepilot/controller/DailyReviewController.java`
- 新建：`src/main/java/com/lifepilot/controller/dto/SaveDailyReviewRequest.java`
- 测试：`src/test/java/com/lifepilot/controller/DailyReviewControllerTest.java`
- 新建：`frontend/src/api/dailyReviews.js`
- 新建：`frontend/src/views/ReviewView.vue`
- 修改：`frontend/src/router/index.js`
- 修改：`frontend/src/App.vue`

**接口：**

| 方法 | 路径 | 行为 |
|---|---|---|
| `POST` | `/api/reviews/daily/{date}/draft` | 创建或刷新复盘草稿 |
| `GET` | `/api/reviews/daily/{date}` | 查看复盘 |
| `PUT` | `/api/reviews/daily/{date}` | 保存编辑后的复盘 |

**页面字段：**

- 已完成工作
- 未完成工作
- 新增任务
- 反思
- 明日计划

### 任务 14：接入 AI 辅助复盘草稿

**文件：**
- 修改：`src/main/java/com/lifepilot/service/DailyReviewService.java`
- 新建：`src/main/java/com/lifepilot/agent/ReviewDraftClient.java`
- 测试：`src/test/java/com/lifepilot/service/DailyReviewServiceTest.java`
- 测试：`src/test/java/com/lifepilot/agent/ReviewDraftClientTest.java`

**要实现的行为：**

- AI 生成复盘成功时，返回 AI 润色后的复盘草稿。
- AI 失败时，仍返回确定性复盘草稿。

**建议流程：**

1. 从 todos 和 execution logs 构造复盘输入。
2. 尝试调用 AI 生成复盘。
3. 如果 AI 抛异常，回退到确定性文本。

**验证命令：**

```powershell
.\gradlew.bat test --tests "*DailyReview*" --tests "*ReviewDraft*"
```

---

## 阶段六：全量验证和文档更新

### 任务 15：全量验证和文档更新

**文件：**
- 修改：`README.md`
- 修改：`docs/01_产品需求.md`
- 修改：`docs/02_系统架构.md`
- 修改：`docs/03_技术规范.md`

**需要补充的文档内容：**

- AI 计划草案和确认流程。
- Today Dashboard 的使用方式。
- 提醒机制。
- 每日复盘流程。
- 新增配置项。

**验证命令：**

```powershell
.\gradlew.bat test
```

```powershell
cd frontend
npm run build
```

```powershell
docker compose up -d postgres redis
.\gradlew.bat bootRun
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

---

## 发布检查清单

- `.\gradlew.bat test` 通过。
- `cd frontend; npm run build` 通过。
- `docker compose up -d postgres redis` 可正常启动依赖。
- 手动验证对话流程：用户描述目标，AI 返回计划草案，用户确认后生成待办。
- 手动验证 Today Dashboard：今日重点、时间线、Inbox、进度正常展示。
- 手动验证提醒流程：到期提醒只出现一次。
- 手动验证复盘流程：可以生成草稿、编辑、保存。
- 请求日志里没有写入密钥或不必要的个人任务内容。
- 浏览器中的中文 UI 文案显示正常。

---

## 推荐执行顺序

1. 任务 1：先修复当前可见体验。
2. 任务 2 到 7：完成 AI 计划草案、确认、落库和前端确认卡片。
3. 任务 8 到 9：完成 Today Dashboard，让计划每天可用。
4. 任务 10 到 11：完成提醒闭环。
5. 任务 12 到 14：完成每日复盘闭环。
6. 任务 15：全量验证并更新文档。

第一个真正可用的里程碑是任务 9 完成后：用户可以在对话里说出目标，确认 AI 拆出的任务计划，然后在 Today Dashboard 看到排好序的今日执行清单。
