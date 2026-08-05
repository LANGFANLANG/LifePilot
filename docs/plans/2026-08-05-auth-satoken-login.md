# Sa-Token 登录与验证码实现计划

**Goal:** 为 LifePilot 添加账号密码登录、注册与算术验证码能力，使用 Sa-Token 管理会话，Token 会话存入 Redis；保留 Spring Security 作为过滤器链基线，Sa-Token 负责业务鉴权。

**Tech Stack:** Java 21, Spring Boot 3.3, Spring Security, Sa-Token (spring-boot3-starter + redis-jackson), easy-captcha, BCrypt (spring-security-crypto), Redis, PostgreSQL/Flyway, Vue 3.

---

## 执行规则

- 每个任务小到可在 2-5 分钟内完成并验证。
- 优先编写/更新聚焦测试。
- 每步改动后运行最窄的测试。
- 保持 DTO 与实体分离；控制器保持薄，业务逻辑在 service。
- 不提交密钥；密码仅存 BCrypt 哈希。

---

## Task 1: 添加依赖

**Files:**
- Edit: `build.gradle`

添加：

```groovy
implementation 'cn.dev33:sa-token-spring-boot3-starter:1.39.0'
implementation 'cn.dev33:sa-token-redis-jackson:1.39.0'
implementation 'com.github.whvcse:easy-captcha:1.6.2'
```

保留 `spring-boot-starter-security` 与 `spring-security-crypto`（随 starter 带入），用于 BCryptPasswordEncoder。

**验证:** `.\gradlew.bat dependencies --configuration runtimeClasspath | Select-String "sa-token|easy-captcha"`

## Task 2: 配置 Sa-Token

**Files:**
- Edit: `src/main/resources/application.yml`
- Edit: `src/main/resources/application-local.yml`

`application.yml` 增加：

```yaml
sa-token:
  token-name: Authorization
  timeout: 86400
  active-timeout: -1
  is-concurrent: true
  is-share: true
  token-style: uuid
  is-log: true
  is-read-cookie: false
  is-read-header: true
  is-read-body: false

lifepilot:
  captcha:
    expire-seconds: 300
```

## Task 3: Flyway V3 迁移

**Files:**
- Create: `src/main/resources/db/migration/V3__create_user_accounts.sql`

```sql
CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(64),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
```

## Task 4: 用户实体与仓库

**Files:**
- Create: `src/main/java/com/lifepilot/domain/UserAccount.java`
- Create: `src/main/java/com/lifepilot/repository/UserAccountRepository.java`

`UserAccount`: UUID id, username, passwordHash, displayName, enabled, createdAt, updatedAt（预更新钩子维护 updatedAt）。
`UserAccountRepository extends JpaRepository<UserAccount, UUID>`，含 `Optional<UserAccount> findByUsername(String username)`、`boolean existsByUsername(String username)`。

## Task 5: CaptchaService

**Files:**
- Create: `src/main/java/com/lifepilot/service/CaptchaService.java`
- Create: `src/main/java/com/lifepilot/service/dto/CaptchaView.java`

- `create()`: 生成算术题（a op b），生成图片 base64，答案存 Redis `auth:captcha:{captchaId}` TTL 5 分钟，返回 `CaptchaView(captchaId, imageBase64)`。
- `verifyAndConsume(captchaId, code)`: 取键并删除（一次性），比对不区分大小写（算术为数字），不匹配抛 `IllegalArgumentException`。

依赖 `StringRedisTemplate`、`SpecCaptcha`（easy-captcha）。

## Task 6: AuthService

**Files:**
- Create: `src/main/java/com/lifepilot/service/AuthService.java`
- Create: `src/main/java/com/lifepilot/service/dto/LoginResult.java`
- Create: `src/main/java/com/lifepilot/service/dto/UserProfile.java`

- `register(username, password, displayName, captchaId, captchaCode)`: 校验验证码 → 查重 → 创建用户（BCrypt 哈希）。
- `login(username, password, captchaId, captchaCode)`: 校验验证码 → 查用户 → 比对密码 → `StpUtil.login(user.id)` → 返回 `LoginResult(token, UserProfile)`。
- `logout()`: `StpUtil.logout()`。
- `currentUser()`: `StpUtil.getLoginIdAsLong/UUID` 查用户。
- 密码错误/用户不存在统一抛 `IllegalArgumentException("用户名或密码错误")`，避免枚举泄露。

## Task 7: AuthController 与 DTO

**Files:**
- Create: `src/main/java/com/lifepilot/controller/AuthController.java`
- Create: `src/main/java/com/lifepilot/controller/dto/LoginRequest.java`
- Create: `src/main/java/com/lifepilot/controller/dto/RegisterRequest.java`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/auth/captcha` | `Result<CaptchaView>` |
| POST | `/api/auth/register` | `Result<UserProfile>` |
| POST | `/api/auth/login` | `Result<LoginResult>` |
| POST | `/api/auth/logout` | `Result<Void>` |
| GET | `/api/auth/me` | `Result<UserProfile>` |

## Task 8: SaTokenConfig + 异常处理 + SecurityConfig 调整

**Files:**
- Create: `src/main/java/com/lifepilot/config/SaTokenConfig.java`
- Create: `src/main/java/com/lifepilot/api/GlobalExceptionHandler.java`（或并入现有结构）
- Edit: `src/main/java/com/lifepilot/config/SecurityConfig.java`

`SaTokenConfig`: 注册 `SaInterceptor`，注入 `authEnabled`；启用时 `/api/**` 需登录，放行 `/api/auth/login`、`/api/auth/register`、`/api/auth/captcha`、`/actuator/**`；关闭时放行。

异常处理: `NotLoginException` → 401 + `Result.failure("UNAUTHORIZED", ...)`；`IllegalArgumentException` → 400；`DataIntegrityViolationException` → 409 用户名冲突。

`SecurityConfig`: 保持过滤器链，`.authorizeHttpRequests` 放开 `/api/auth/**`，避免与 Sa-Token 拦截器双重拦截冲突；其余保持现状。

## Task 9: 后端测试

**Files:**
- Create: `src/test/java/com/lifepilot/service/CaptchaServiceTest.java`
- Create: `src/test/java/com/lifepilot/service/AuthServiceTest.java`
- Create: `src/test/java/com/lifepilot/controller/AuthControllerTest.java`
- Edit/迁移: `src/test/java/com/lifepilot/config/SecurityConfigTest.java`

固定时钟与确定性数据；Mockito 模拟仓库；Controller 测试使用 MockMvc + Sa-Token mock（`StpUtil` 相关）。

**验证:** `.\gradlew.bat test --tests com.lifepilot.service.CaptchaServiceTest`

## Task 10: 前端登录

**Files:**
- Create: `frontend/src/views/LoginView.vue`
- Create: `frontend/src/api/auth.js`
- Edit: `frontend/src/router/index.js`（加 `/login` 路由 + 全局守卫）
- Edit: `frontend/src/api/http.js`（请求带 Authorization token，401 清 token 跳登录）

token 存 `localStorage`，键 `lifepilot_token`。

## Task 11: 全量验证

- `.\gradlew.bat test`
- `docker compose up -d postgres redis` + `.\gradlew.bat bootRun` 手工验证登录/注册/验证码
