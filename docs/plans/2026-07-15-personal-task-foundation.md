# Personal Task Foundation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Upgrade todos into schedulable personal execution tasks with richer metadata plus edit and delete APIs.

**Architecture:** Keep the existing layered Spring Boot shape: controller request DTOs map to service commands, the service owns mutations, and `TodoView` exposes the API shape. Add a Flyway V2 migration for persisted fields and keep AI tool methods thin wrappers over `TodoService`.

**Tech Stack:** Java 21, Spring Boot 3.3, Spring Data JPA, Flyway, JUnit 5, Mockito, MockMvc, vanilla HTML/CSS/JS frontend.

---

### Task 1: Extend Todo Metadata

**Files:**
- Create: `src/main/java/com/lifepilot/domain/TodoPriority.java`
- Modify: `src/main/java/com/lifepilot/domain/Todo.java`
- Modify: `src/main/java/com/lifepilot/service/dto/CreateTodoCommand.java`
- Modify: `src/main/java/com/lifepilot/service/dto/TodoView.java`
- Create: `src/main/resources/db/migration/V2__extend_todo_execution_fields.sql`
- Test: `src/test/java/com/lifepilot/domain/TodoTest.java`
- Test: `src/test/java/com/lifepilot/service/TodoServiceTest.java`

**Steps:**
1. Add failing tests for default priority and optional execution fields.
2. Add `TodoPriority` and fields: priority, category, estimatedMinutes, plannedStartAt, reminderAt, completedAt, parentTodoId, source, postponementCount.
3. Add V2 migration columns with safe defaults.
4. Update create command and view mapping.
5. Run `.\gradlew.bat test --tests "*Todo*"` and expect pass.

### Task 2: Add Update and Delete APIs

**Files:**
- Create: `src/main/java/com/lifepilot/controller/dto/UpdateTodoRequest.java`
- Create: `src/main/java/com/lifepilot/service/dto/UpdateTodoCommand.java`
- Modify: `src/main/java/com/lifepilot/controller/TodoController.java`
- Modify: `src/main/java/com/lifepilot/service/TodoService.java`
- Test: `src/test/java/com/lifepilot/controller/TodoControllerTest.java`
- Test: `src/test/java/com/lifepilot/service/TodoServiceTest.java`

**Steps:**
1. Add tests for `PUT /api/todos/{id}` and `DELETE /api/todos/{id}`.
2. Implement `Todo.update(...)` and `TodoService.update/delete`.
3. Wire controller methods.
4. Run controller and service tests.

### Task 3: Sync AI Tool and Frontend

**Files:**
- Modify: `src/main/java/com/lifepilot/tool/TodoTool.java`
- Modify: `src/test/java/com/lifepilot/tool/TodoToolTest.java`
- Modify: `src/main/resources/static/index.html`
- Modify: `src/main/resources/static/app.js`
- Modify: `src/main/resources/static/styles.css`

**Steps:**
1. Extend AI todo creation parameters and add update/delete tool methods.
2. Show priority/category/planned/reminder metadata in the dashboard.
3. Add lightweight edit/delete interactions.
4. Run focused tests and static page test.

### Task 4: Full Verification

**Files:**
- Existing test suite.

**Steps:**
1. Run `.\gradlew.bat test`.
2. Fix failures without changing unrelated user edits.
3. Report remaining uncommitted files clearly.
