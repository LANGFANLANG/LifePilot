# LifePilot MVP Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the LifePilot Personal Agent MVP with AI chat, todos, notes, chat memory, tool calling, and execution logs.

**Architecture:** Start with a small Spring Boot 3 application and add one vertical slice at a time. Keep controllers thin, put business rules in services, persist domain data through repositories, and expose selected service actions as Spring AI tools through dedicated tool classes.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring AI, Spring Security, JWT, PostgreSQL, Flyway, Redis, JUnit 5, Mockito, Testcontainers, SLF4J, Micrometer, OpenTelemetry.

---

## Execution Rules

- One task should be small enough to finish and verify in 2-5 minutes.
- Write or update a focused test before implementation whenever practical.
- Run the narrowest possible test after each change.
- Commit after each coherent task once the repository is initialized.
- Do not add RAG, MCP, Calendar, Daily Review, proactive reminders, or email summary in MVP unless a task below explicitly prepares an extension point.
- Keep DTOs separate from entities.
- Keep one tool capability in one tool class.
- Add second confirmation before any high-risk tool action. MVP tools should avoid high-risk actions by default.

---

## Phase 0: Repository And Project Skeleton

### Task 1: Initialize Git Repository

**Files:**
- Create: `.gitignore`

**Step 1: Check repository state**

Run:

```powershell
git status
```

Expected: fails with `fatal: not a git repository`.

**Step 2: Initialize repository**

Run:

```powershell
git init
```

Expected: repository initialized.

**Step 3: Create `.gitignore`**

Create `.gitignore` with:

```gitignore
.gradle/
build/
out/
.idea/
*.iml
*.log
.env
.env.*
!.env.example
```

**Step 4: Verify**

Run:

```powershell
git status --short
```

Expected: `.gitignore` and `docs/` are untracked.

**Step 5: Commit**

Run:

```powershell
git add .gitignore docs
git commit -m "docs: add initial project documentation"
```

Expected: commit succeeds.

---

### Task 2: Create Gradle Spring Boot Build

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`

**Step 1: Create `settings.gradle`**

```gradle
pluginManagement {
    repositories {
        maven { url = 'https://repo.spring.io/milestone' }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = 'https://repo.spring.io/milestone' }
        mavenCentral()
    }
}

rootProject.name = 'lifepilot'
```

**Step 2: Create `build.gradle`**

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.5'
    id 'io.spring.dependency-management' version '1.1.6'
}

group = 'com.lifepilot'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

ext {
    springAiVersion = '1.0.0-M3'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'

    runtimeOnly 'org.postgresql:postgresql'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:postgresql'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**Step 3: Verify dependency resolution**

Run:

```powershell
gradle dependencies --configuration testRuntimeClasspath
```

Expected: dependency tree prints without errors.

**Step 4: Commit**

Run:

```powershell
git add settings.gradle build.gradle
git commit -m "chore: add spring boot gradle build"
```

Expected: commit succeeds.

---

### Task 3: Add Application Entry Point

**Files:**
- Create: `src/main/java/com/lifepilot/LifePilotApplication.java`
- Create: `src/test/java/com/lifepilot/LifePilotApplicationTests.java`

**Step 1: Write context load test**

```java
package com.lifepilot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LifePilotApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

**Step 2: Run test and verify it fails**

Run:

```powershell
gradle test --tests com.lifepilot.LifePilotApplicationTests
```

Expected: FAIL because application class does not exist.

**Step 3: Create application class**

```java
package com.lifepilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LifePilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifePilotApplication.class, args);
    }
}
```

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.LifePilotApplicationTests
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src
git commit -m "feat: add application entry point"
```

Expected: commit succeeds.

---

## Phase 1: Shared API Foundation

### Task 4: Add Unified Result Response

**Files:**
- Create: `src/main/java/com/lifepilot/api/Result.java`
- Create: `src/test/java/com/lifepilot/api/ResultTest.java`

**Step 1: Write tests**

```java
package com.lifepilot.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void successWrapsData() {
        Result<String> result = Result.success("ok");
        assertThat(result.success()).isTrue();
        assertThat(result.code()).isEqualTo("OK");
        assertThat(result.data()).isEqualTo("ok");
    }

    @Test
    void failureWrapsError() {
        Result<Void> result = Result.failure("BAD_REQUEST", "bad input");
        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("BAD_REQUEST");
        assertThat(result.message()).isEqualTo("bad input");
    }
}
```

**Step 2: Run test and verify it fails**

Run:

```powershell
gradle test --tests com.lifepilot.api.ResultTest
```

Expected: FAIL because `Result` does not exist.

**Step 3: Implement `Result`**

```java
package com.lifepilot.api;

public record Result<T>(boolean success, String code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(true, "OK", "success", data);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, code, message, null);
    }
}
```

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.api.ResultTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/api/Result.java src/test/java/com/lifepilot/api/ResultTest.java
git commit -m "feat: add unified api result"
```

Expected: commit succeeds.

---

### Task 5: Add Global Exception Handling

**Files:**
- Create: `src/main/java/com/lifepilot/api/GlobalExceptionHandler.java`
- Create: `src/main/java/com/lifepilot/api/ErrorCode.java`
- Create: `src/test/java/com/lifepilot/api/GlobalExceptionHandlerTest.java`

**Step 1: Write handler unit test**

```java
package com.lifepilot.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handlesIllegalArgumentException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Result<Void>> response = handler.handleIllegalArgument(
                new IllegalArgumentException("invalid")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
    }
}
```

**Step 2: Run test and verify it fails**

Run:

```powershell
gradle test --tests com.lifepilot.api.GlobalExceptionHandlerTest
```

Expected: FAIL because handler does not exist.

**Step 3: Implement error code enum**

```java
package com.lifepilot.api;

public enum ErrorCode {
    BAD_REQUEST,
    UNAUTHORIZED,
    NOT_FOUND,
    INTERNAL_ERROR
}
```

**Step 4: Implement handler**

```java
package com.lifepilot.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Result.failure(ErrorCode.BAD_REQUEST.name(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(ErrorCode.INTERNAL_ERROR.name(), "internal error"));
    }
}
```

**Step 5: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.api.GlobalExceptionHandlerTest
```

Expected: PASS.

**Step 6: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/api src/test/java/com/lifepilot/api
git commit -m "feat: add global exception handling"
```

Expected: commit succeeds.

---

### Task 6: Add Application Configuration Files

**Files:**
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`
- Create: `.env.example`

**Step 1: Create base config**

```yaml
spring:
  application:
    name: lifepilot
  profiles:
    active: local

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

**Step 2: Create local config**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lifepilot
    username: lifepilot
    password: lifepilot
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
  data:
    redis:
      host: localhost
      port: 6379
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}

lifepilot:
  security:
    jwt-secret: ${LIFEPILOT_JWT_SECRET:change-me-change-me-change-me-change-me}
```

**Step 3: Create `.env.example`**

```dotenv
OPENAI_API_KEY=
LIFEPILOT_JWT_SECRET=replace-with-a-long-random-secret
```

**Step 4: Run tests**

Run:

```powershell
gradle test
```

Expected: PASS or fails only because PostgreSQL/Redis are unavailable. If it fails because external services are unavailable, postpone full context test until Testcontainers config is added.

**Step 5: Commit**

Run:

```powershell
git add src/main/resources .env.example
git commit -m "chore: add application configuration"
```

Expected: commit succeeds.

---

## Phase 2: Database Foundation

### Task 7: Add Docker Compose For Local Dependencies

**Files:**
- Create: `docker-compose.yml`

**Step 1: Create compose file**

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: lifepilot
      POSTGRES_USER: lifepilot
      POSTGRES_PASSWORD: lifepilot
    ports:
      - "5432:5432"
    volumes:
      - lifepilot-postgres:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  lifepilot-postgres:
```

**Step 2: Validate compose syntax**

Run:

```powershell
docker compose config
```

Expected: normalized compose config prints.

**Step 3: Commit**

Run:

```powershell
git add docker-compose.yml
git commit -m "chore: add local postgres and redis"
```

Expected: commit succeeds.

---

### Task 8: Add Initial Flyway Migration

**Files:**
- Create: `src/main/resources/db/migration/V1__init_core_tables.sql`

**Step 1: Create migration**

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    role VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE todos (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(40) NOT NULL,
    due_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE notes (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE execution_logs (
    id UUID PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id),
    action_type VARCHAR(80) NOT NULL,
    input TEXT,
    output TEXT,
    status VARCHAR(40) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL
);
```

**Step 2: Start dependencies**

Run:

```powershell
docker compose up -d postgres redis
```

Expected: containers start.

**Step 3: Run app migration check**

Run:

```powershell
gradle bootRun
```

Expected: Flyway applies migration and app starts. Stop app after startup.

**Step 4: Commit**

Run:

```powershell
git add src/main/resources/db/migration/V1__init_core_tables.sql
git commit -m "feat: add initial database schema"
```

Expected: commit succeeds.

---

## Phase 3: Todo Vertical Slice

### Task 9: Add Todo Domain Entity

**Files:**
- Create: `src/main/java/com/lifepilot/domain/TodoStatus.java`
- Create: `src/main/java/com/lifepilot/domain/Todo.java`
- Create: `src/test/java/com/lifepilot/domain/TodoTest.java`

**Step 1: Write domain test**

```java
package com.lifepilot.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TodoTest {

    @Test
    void createPendingTodo() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);
        assertThat(todo.getTitle()).isEqualTo("Buy milk");
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.PENDING);
        assertThat(todo.getId()).isNotNull();
    }
}
```

**Step 2: Run test and verify it fails**

Run:

```powershell
gradle test --tests com.lifepilot.domain.TodoTest
```

Expected: FAIL because `Todo` does not exist.

**Step 3: Implement enum and entity**

Implement only fields from migration plus `create`, `complete`, and getters.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.domain.TodoTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/domain/TodoStatus.java src/main/java/com/lifepilot/domain/Todo.java src/test/java/com/lifepilot/domain/TodoTest.java
git commit -m "feat: add todo domain"
```

Expected: commit succeeds.

---

### Task 10: Add Todo Repository

**Files:**
- Create: `src/main/java/com/lifepilot/repository/TodoRepository.java`
- Create: `src/test/java/com/lifepilot/repository/TodoRepositoryTest.java`

**Step 1: Write repository test**

Create a `@DataJpaTest` that saves a pending todo and reads it by id.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.repository.TodoRepositoryTest
```

Expected: FAIL until repository and test database configuration are complete.

**Step 3: Implement repository**

```java
package com.lifepilot.repository;

import com.lifepilot.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {
}
```

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.repository.TodoRepositoryTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/repository/TodoRepository.java src/test/java/com/lifepilot/repository/TodoRepositoryTest.java
git commit -m "feat: add todo repository"
```

Expected: commit succeeds.

---

### Task 11: Add Todo Service

**Files:**
- Create: `src/main/java/com/lifepilot/service/TodoService.java`
- Create: `src/main/java/com/lifepilot/service/dto/CreateTodoCommand.java`
- Create: `src/main/java/com/lifepilot/service/dto/TodoView.java`
- Create: `src/test/java/com/lifepilot/service/TodoServiceTest.java`

**Step 1: Write service tests**

Test:
- creates todo
- lists todos
- completes todo
- throws `IllegalArgumentException` when completing missing todo

**Step 2: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.service.TodoServiceTest
```

Expected: FAIL because service does not exist.

**Step 3: Implement minimal service**

Use `TodoRepository`. Return DTOs, not entities.

**Step 4: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.service.TodoServiceTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/service src/test/java/com/lifepilot/service/TodoServiceTest.java
git commit -m "feat: add todo service"
```

Expected: commit succeeds.

---

### Task 12: Add Todo REST Controller

**Files:**
- Create: `src/main/java/com/lifepilot/controller/TodoController.java`
- Create: `src/main/java/com/lifepilot/controller/dto/CreateTodoRequest.java`
- Create: `src/test/java/com/lifepilot/controller/TodoControllerTest.java`

**Step 1: Write controller tests**

Use `@WebMvcTest`.

Test:
- `POST /api/todos` returns `Result<TodoView>`
- `GET /api/todos` returns list
- `POST /api/todos/{id}/complete` returns completed todo

**Step 2: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.controller.TodoControllerTest
```

Expected: FAIL because controller does not exist.

**Step 3: Implement controller**

Controller should only validate request and call `TodoService`.

**Step 4: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.controller.TodoControllerTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/controller src/test/java/com/lifepilot/controller/TodoControllerTest.java
git commit -m "feat: add todo api"
```

Expected: commit succeeds.

---

## Phase 4: Notes Vertical Slice

### Task 13: Add Note Domain And Repository

**Files:**
- Create: `src/main/java/com/lifepilot/domain/Note.java`
- Create: `src/main/java/com/lifepilot/repository/NoteRepository.java`
- Create: `src/test/java/com/lifepilot/domain/NoteTest.java`

**Step 1: Write domain test**

Test creating a note with title and content.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.domain.NoteTest
```

Expected: FAIL.

**Step 3: Implement note entity and repository**

Use fields from `notes` table.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.domain.NoteTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/domain/Note.java src/main/java/com/lifepilot/repository/NoteRepository.java src/test/java/com/lifepilot/domain/NoteTest.java
git commit -m "feat: add note domain"
```

Expected: commit succeeds.

---

### Task 14: Add Note Service And Controller

**Files:**
- Create: `src/main/java/com/lifepilot/service/NoteService.java`
- Create: `src/main/java/com/lifepilot/service/dto/CreateNoteCommand.java`
- Create: `src/main/java/com/lifepilot/service/dto/NoteView.java`
- Create: `src/main/java/com/lifepilot/controller/NoteController.java`
- Create: `src/main/java/com/lifepilot/controller/dto/CreateNoteRequest.java`
- Create: `src/test/java/com/lifepilot/service/NoteServiceTest.java`
- Create: `src/test/java/com/lifepilot/controller/NoteControllerTest.java`

**Step 1: Write service tests**

Test:
- creates note
- lists notes ordered by update time
- gets note by id
- throws when note is missing

**Step 2: Implement service**

Use repository and DTOs only.

**Step 3: Run service tests**

Run:

```powershell
gradle test --tests com.lifepilot.service.NoteServiceTest
```

Expected: PASS.

**Step 4: Write controller tests**

Test:
- `POST /api/notes`
- `GET /api/notes`
- `GET /api/notes/{id}`

**Step 5: Implement controller**

Controller only delegates to service.

**Step 6: Run controller tests**

Run:

```powershell
gradle test --tests com.lifepilot.controller.NoteControllerTest
```

Expected: PASS.

**Step 7: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/service src/main/java/com/lifepilot/controller src/test/java/com/lifepilot/service src/test/java/com/lifepilot/controller
git commit -m "feat: add note api"
```

Expected: commit succeeds.

---

## Phase 5: Chat Memory And Agent

### Task 15: Add Conversation And Message Domain

**Files:**
- Create: `src/main/java/com/lifepilot/domain/Conversation.java`
- Create: `src/main/java/com/lifepilot/domain/ChatMessage.java`
- Create: `src/main/java/com/lifepilot/domain/ChatRole.java`
- Create: `src/main/java/com/lifepilot/repository/ConversationRepository.java`
- Create: `src/main/java/com/lifepilot/repository/ChatMessageRepository.java`
- Create: `src/test/java/com/lifepilot/domain/ConversationTest.java`

**Step 1: Write domain test**

Test creating a conversation and adding user/assistant messages.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.domain.ConversationTest
```

Expected: FAIL.

**Step 3: Implement domain and repositories**

Keep message role as enum: `USER`, `ASSISTANT`, `SYSTEM`, `TOOL`.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.domain.ConversationTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/domain src/main/java/com/lifepilot/repository src/test/java/com/lifepilot/domain/ConversationTest.java
git commit -m "feat: add chat memory domain"
```

Expected: commit succeeds.

---

### Task 16: Add Chat Memory Service

**Files:**
- Create: `src/main/java/com/lifepilot/memory/ChatMemoryService.java`
- Create: `src/main/java/com/lifepilot/memory/dto/ConversationView.java`
- Create: `src/main/java/com/lifepilot/memory/dto/MessageView.java`
- Create: `src/test/java/com/lifepilot/memory/ChatMemoryServiceTest.java`

**Step 1: Write tests**

Test:
- creates conversation
- appends user message
- appends assistant message
- loads recent messages for a conversation

**Step 2: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.memory.ChatMemoryServiceTest
```

Expected: FAIL.

**Step 3: Implement service**

Use repositories and return DTOs. Keep token-window trimming out of MVP.

**Step 4: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.memory.ChatMemoryServiceTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/memory src/test/java/com/lifepilot/memory
git commit -m "feat: add chat memory service"
```

Expected: commit succeeds.

---

### Task 17: Add Agent Service With Mockable AI Gateway

**Files:**
- Create: `src/main/java/com/lifepilot/agent/AiClient.java`
- Create: `src/main/java/com/lifepilot/agent/AgentService.java`
- Create: `src/main/java/com/lifepilot/agent/dto/AgentRequest.java`
- Create: `src/main/java/com/lifepilot/agent/dto/AgentResponse.java`
- Create: `src/test/java/com/lifepilot/agent/AgentServiceTest.java`

**Step 1: Write service test**

Test:
- user message is saved
- AI client is called with recent memory
- assistant response is saved
- response includes conversation id

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.agent.AgentServiceTest
```

Expected: FAIL.

**Step 3: Implement `AiClient` interface**

```java
package com.lifepilot.agent;

import com.lifepilot.memory.dto.MessageView;

import java.util.List;

public interface AiClient {
    String chat(List<MessageView> messages);
}
```

**Step 4: Implement `AgentService`**

Use `ChatMemoryService` and `AiClient`.

**Step 5: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.agent.AgentServiceTest
```

Expected: PASS.

**Step 6: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/agent src/test/java/com/lifepilot/agent
git commit -m "feat: add agent service"
```

Expected: commit succeeds.

---

### Task 18: Add Spring AI Client Adapter

**Files:**
- Create: `src/main/java/com/lifepilot/agent/SpringAiClient.java`
- Create: `src/test/java/com/lifepilot/agent/SpringAiClientTest.java`

**Step 1: Write adapter test**

Use a mocked `ChatClient` or a small wrapper seam so the test verifies message conversion without calling the network.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.agent.SpringAiClientTest
```

Expected: FAIL.

**Step 3: Implement adapter**

Convert `MessageView` roles into Spring AI prompt messages and call `ChatClient`.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.agent.SpringAiClientTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/agent/SpringAiClient.java src/test/java/com/lifepilot/agent/SpringAiClientTest.java
git commit -m "feat: connect agent to spring ai"
```

Expected: commit succeeds.

---

### Task 19: Add Chat REST API

**Files:**
- Create: `src/main/java/com/lifepilot/controller/ChatController.java`
- Create: `src/main/java/com/lifepilot/controller/dto/ChatRequest.java`
- Create: `src/test/java/com/lifepilot/controller/ChatControllerTest.java`

**Step 1: Write controller test**

Test `POST /api/chat` returns `Result<AgentResponse>`.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.controller.ChatControllerTest
```

Expected: FAIL.

**Step 3: Implement controller**

Validate message is not blank. Delegate to `AgentService`.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.controller.ChatControllerTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/controller/ChatController.java src/main/java/com/lifepilot/controller/dto/ChatRequest.java src/test/java/com/lifepilot/controller/ChatControllerTest.java
git commit -m "feat: add chat api"
```

Expected: commit succeeds.

---

## Phase 6: Tool Calling

### Task 20: Add Tool Result Contract

**Files:**
- Create: `src/main/java/com/lifepilot/tool/ToolResult.java`
- Create: `src/test/java/com/lifepilot/tool/ToolResultTest.java`

**Step 1: Write test**

Test success and failure tool result creation.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.tool.ToolResultTest
```

Expected: FAIL.

**Step 3: Implement record**

```java
package com.lifepilot.tool;

public record ToolResult(boolean success, String message, Object data) {

    public static ToolResult success(String message, Object data) {
        return new ToolResult(true, message, data);
    }

    public static ToolResult failure(String message) {
        return new ToolResult(false, message, null);
    }
}
```

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.tool.ToolResultTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/tool/ToolResult.java src/test/java/com/lifepilot/tool/ToolResultTest.java
git commit -m "feat: add tool result contract"
```

Expected: commit succeeds.

---

### Task 21: Add Todo Tool

**Files:**
- Create: `src/main/java/com/lifepilot/tool/TodoTool.java`
- Create: `src/test/java/com/lifepilot/tool/TodoToolTest.java`

**Step 1: Write tests**

Test:
- creates todo through service
- lists todos through service
- completes todo through service

**Step 2: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.tool.TodoToolTest
```

Expected: FAIL.

**Step 3: Implement tool**

Use one public method per tool action and delegate to `TodoService`. Annotate methods with Spring AI tool annotations supported by the selected Spring AI version.

**Step 4: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.tool.TodoToolTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/tool/TodoTool.java src/test/java/com/lifepilot/tool/TodoToolTest.java
git commit -m "feat: add todo ai tool"
```

Expected: commit succeeds.

---

### Task 22: Add Note Tool

**Files:**
- Create: `src/main/java/com/lifepilot/tool/NoteTool.java`
- Create: `src/test/java/com/lifepilot/tool/NoteToolTest.java`

**Step 1: Write tests**

Test:
- creates note through service
- lists notes through service
- gets note through service

**Step 2: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.tool.NoteToolTest
```

Expected: FAIL.

**Step 3: Implement tool**

Use one public method per note action and delegate to `NoteService`.

**Step 4: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.tool.NoteToolTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/tool/NoteTool.java src/test/java/com/lifepilot/tool/NoteToolTest.java
git commit -m "feat: add note ai tool"
```

Expected: commit succeeds.

---

### Task 23: Wire Tools Into Spring AI Client

**Files:**
- Modify: `src/main/java/com/lifepilot/agent/SpringAiClient.java`
- Create: `src/main/java/com/lifepilot/config/AiToolConfig.java`
- Modify: `src/test/java/com/lifepilot/agent/SpringAiClientTest.java`

**Step 1: Update test**

Verify the AI client is constructed with `TodoTool` and `NoteTool` available.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.agent.SpringAiClientTest
```

Expected: FAIL.

**Step 3: Implement tool wiring**

Register `TodoTool` and `NoteTool` with the Spring AI chat client builder according to the selected Spring AI version.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.agent.SpringAiClientTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/agent/SpringAiClient.java src/main/java/com/lifepilot/config/AiToolConfig.java src/test/java/com/lifepilot/agent/SpringAiClientTest.java
git commit -m "feat: wire ai tools"
```

Expected: commit succeeds.

---

## Phase 7: Execution Logs

### Task 24: Add Execution Log Domain And Repository

**Files:**
- Create: `src/main/java/com/lifepilot/domain/ExecutionLog.java`
- Create: `src/main/java/com/lifepilot/domain/ExecutionStatus.java`
- Create: `src/main/java/com/lifepilot/repository/ExecutionLogRepository.java`
- Create: `src/test/java/com/lifepilot/domain/ExecutionLogTest.java`

**Step 1: Write domain test**

Test creating success and failure execution logs.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.domain.ExecutionLogTest
```

Expected: FAIL.

**Step 3: Implement entity, enum, repository**

Use fields from `execution_logs` table.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.domain.ExecutionLogTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/domain/ExecutionLog.java src/main/java/com/lifepilot/domain/ExecutionStatus.java src/main/java/com/lifepilot/repository/ExecutionLogRepository.java src/test/java/com/lifepilot/domain/ExecutionLogTest.java
git commit -m "feat: add execution log domain"
```

Expected: commit succeeds.

---

### Task 25: Add Execution Log Service

**Files:**
- Create: `src/main/java/com/lifepilot/service/ExecutionLogService.java`
- Create: `src/main/java/com/lifepilot/service/dto/ExecutionLogView.java`
- Create: `src/test/java/com/lifepilot/service/ExecutionLogServiceTest.java`

**Step 1: Write tests**

Test:
- records success action
- records failed action
- lists recent logs

**Step 2: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.service.ExecutionLogServiceTest
```

Expected: FAIL.

**Step 3: Implement service**

Use repository and DTOs.

**Step 4: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.service.ExecutionLogServiceTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/service/ExecutionLogService.java src/main/java/com/lifepilot/service/dto/ExecutionLogView.java src/test/java/com/lifepilot/service/ExecutionLogServiceTest.java
git commit -m "feat: add execution log service"
```

Expected: commit succeeds.

---

### Task 26: Log Agent And Tool Executions

**Files:**
- Modify: `src/main/java/com/lifepilot/agent/AgentService.java`
- Modify: `src/main/java/com/lifepilot/tool/TodoTool.java`
- Modify: `src/main/java/com/lifepilot/tool/NoteTool.java`
- Modify: `src/test/java/com/lifepilot/agent/AgentServiceTest.java`
- Modify: `src/test/java/com/lifepilot/tool/TodoToolTest.java`
- Modify: `src/test/java/com/lifepilot/tool/NoteToolTest.java`

**Step 1: Update tests**

Assert that successful agent calls and tool calls create execution logs.

**Step 2: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.agent.AgentServiceTest --tests com.lifepilot.tool.TodoToolTest --tests com.lifepilot.tool.NoteToolTest
```

Expected: FAIL.

**Step 3: Implement logging**

Inject `ExecutionLogService`. Record success and failure around agent/tool operations.

**Step 4: Run tests**

Run:

```powershell
gradle test --tests com.lifepilot.agent.AgentServiceTest --tests com.lifepilot.tool.TodoToolTest --tests com.lifepilot.tool.NoteToolTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/agent src/main/java/com/lifepilot/tool src/test/java/com/lifepilot/agent src/test/java/com/lifepilot/tool
git commit -m "feat: record agent and tool execution logs"
```

Expected: commit succeeds.

---

## Phase 8: Security

### Task 27: Add Passwordless Dev Security Baseline

**Files:**
- Create: `src/main/java/com/lifepilot/config/SecurityConfig.java`
- Create: `src/test/java/com/lifepilot/config/SecurityConfigTest.java`

**Step 1: Write security test**

For MVP local development, assert `/actuator/health` is public and `/api/chat` requires authentication only if JWT is enabled.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.config.SecurityConfigTest
```

Expected: FAIL.

**Step 3: Implement minimal security config**

Start with stateless sessions, CSRF disabled for API, health endpoint public. Add a property flag to allow local dev without auth.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.config.SecurityConfigTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/config/SecurityConfig.java src/test/java/com/lifepilot/config/SecurityConfigTest.java
git commit -m "feat: add api security baseline"
```

Expected: commit succeeds.

---

### Task 28: Add JWT Token Service

**Files:**
- Create: `src/main/java/com/lifepilot/service/JwtService.java`
- Create: `src/test/java/com/lifepilot/service/JwtServiceTest.java`

**Step 1: Write token tests**

Test:
- creates token with subject
- parses subject from token
- rejects invalid token

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.service.JwtServiceTest
```

Expected: FAIL.

**Step 3: Implement token service**

Use configured secret. Keep expiry short and configurable.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.service.JwtServiceTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/service/JwtService.java src/test/java/com/lifepilot/service/JwtServiceTest.java
git commit -m "feat: add jwt service"
```

Expected: commit succeeds.

---

## Phase 9: Observability

### Task 29: Add Request Logging Filter

**Files:**
- Create: `src/main/java/com/lifepilot/observability/RequestLoggingFilter.java`
- Create: `src/test/java/com/lifepilot/observability/RequestLoggingFilterTest.java`

**Step 1: Write filter test**

Verify each request gets a request id and logs method/path/status.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.observability.RequestLoggingFilterTest
```

Expected: FAIL.

**Step 3: Implement filter**

Use SLF4J and MDC. Do not log request body.

**Step 4: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.observability.RequestLoggingFilterTest
```

Expected: PASS.

**Step 5: Commit**

Run:

```powershell
git add src/main/java/com/lifepilot/observability src/test/java/com/lifepilot/observability
git commit -m "feat: add request logging"
```

Expected: commit succeeds.

---

### Task 30: Verify Actuator Metrics

**Files:**
- Modify: `src/test/java/com/lifepilot/LifePilotApplicationTests.java`

**Step 1: Add health endpoint test**

Use `MockMvc` to assert `GET /actuator/health` returns 200.

**Step 2: Run test**

Run:

```powershell
gradle test --tests com.lifepilot.LifePilotApplicationTests
```

Expected: PASS.

**Step 3: Commit**

Run:

```powershell
git add src/test/java/com/lifepilot/LifePilotApplicationTests.java
git commit -m "test: verify actuator health"
```

Expected: commit succeeds.

---

## Phase 10: End-To-End MVP Verification

### Task 31: Add Local Smoke Test Script

**Files:**
- Create: `scripts/smoke-test.ps1`

**Step 1: Create script**

Script should:
- call `GET /actuator/health`
- create todo
- list todos
- create note
- list notes
- send one chat message

**Step 2: Run script against local app**

Run:

```powershell
.\scripts\smoke-test.ps1
```

Expected: all calls return successful API result.

**Step 3: Commit**

Run:

```powershell
git add scripts/smoke-test.ps1
git commit -m "test: add local smoke test"
```

Expected: commit succeeds.

---

### Task 32: Add README With MVP Runbook

**Files:**
- Create: `README.md`

**Step 1: Document prerequisites**

Include:
- Java 21
- Gradle
- Docker
- OpenAI API key

**Step 2: Document local startup**

Include:

```powershell
docker compose up -d postgres redis
gradle bootRun
```

**Step 3: Document test commands**

Include:

```powershell
gradle test
.\scripts\smoke-test.ps1
```

**Step 4: Commit**

Run:

```powershell
git add README.md
git commit -m "docs: add mvp runbook"
```

Expected: commit succeeds.

---

### Task 33: Run Full Verification

**Files:**
- No code changes expected.

**Step 1: Run unit and slice tests**

Run:

```powershell
gradle test
```

Expected: PASS.

**Step 2: Start dependencies**

Run:

```powershell
docker compose up -d postgres redis
```

Expected: dependencies are healthy.

**Step 3: Start app**

Run:

```powershell
gradle bootRun
```

Expected: app starts without Flyway or bean errors.

**Step 4: Run smoke test**

Run in another terminal:

```powershell
.\scripts\smoke-test.ps1
```

Expected: all MVP endpoints work.

**Step 5: Final commit if needed**

Run:

```powershell
git status --short
```

Expected: clean working tree.

---

## Recommended Implementation Order

1. Phase 0: repository and skeleton
2. Phase 1: shared API foundation
3. Phase 2: database foundation
4. Phase 3: Todo vertical slice
5. Phase 4: Notes vertical slice
6. Phase 5: Chat memory and agent
7. Phase 6: Tool calling
8. Phase 7: execution logs
9. Phase 8: security
10. Phase 9: observability
11. Phase 10: end-to-end verification

## MVP Done Definition

- `gradle test` passes.
- `docker compose up -d postgres redis` starts local dependencies.
- `gradle bootRun` starts the app.
- Smoke test can create/list todos, create/list notes, and send one chat message.
- Chat messages are stored in PostgreSQL.
- Tool calls can create todos and notes.
- Agent and tool executions are written to `execution_logs`.
- Controller code has no business logic.
- Service DTOs and JPA entities are separate.
- README explains how to run and test the MVP.
