# Repository Guidelines

## Project Structure & Module Organization

LifePilot is a Java 21 Spring Boot application. Production code lives under `src/main/java/com/lifepilot`, organized by responsibility: `controller`, `service`, `repository`, `domain`, `agent`, `tool`, `memory`, `config`, and `api`. Keep HTTP DTOs in `controller/dto` and service-facing records in `service/dto`; do not expose JPA entities directly.

Configuration and database migrations are in `src/main/resources`. Add schema changes as new versioned Flyway files under `db/migration` (for example, `V2__add_todo_index.sql`). The dependency-free frontend is in `src/main/resources/static`. Tests mirror the production package structure under `src/test/java`. Product and architecture notes live in `docs`, while operational scripts belong in `scripts`.

## Build, Test, and Development Commands

On Windows PowerShell, use the checked-in Gradle Wrapper:

- `docker compose up -d postgres redis` starts local PostgreSQL/pgvector and Redis.
- `.\gradlew.bat bootRun` starts the application at `http://localhost:8081`.
- `.\gradlew.bat test` runs the complete automated test suite.
- `.\gradlew.bat test --tests com.lifepilot.tool.DateTimeToolTest` runs one test class.
- `.\gradlew.bat javadoc` verifies and generates API documentation.
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1` checks a running instance.

## Coding Style & Naming Conventions

Use four-space indentation and standard Java conventions: `UpperCamelCase` classes, `lowerCamelCase` members, and `UPPER_SNAKE_CASE` constants. Prefer constructor injection, immutable records for DTOs, and small single-purpose services. Controllers should validate and delegate; business logic belongs in services. Name Spring AI integrations `*Tool` and keep one capability area per tool class. Add concise Javadoc to public types and public methods. Keep source files UTF-8.

## Testing Guidelines

Tests use JUnit 5, AssertJ, Mockito, Spring Boot Test, MockMvc, and Testcontainers. Name classes `*Test` and methods by observable behavior, such as `returnsCurrentDateTimeUsingConfiguredClock`. Cover success and failure paths for services and tools. Use fixed clocks and deterministic data instead of current time or random external services. Run the full suite before submitting changes.

## Commit & Pull Request Guidelines

History follows Conventional Commit prefixes: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`, and `chore:`. Keep each commit focused and use an imperative summary. Pull requests should explain the problem and solution, list verification commands, link relevant issues, and include screenshots for UI changes. Call out migrations or configuration changes explicitly.

## Security & Configuration

Never commit API keys or production credentials. Copy `.env.example` and provide `DEEPSEEK_V4` and `LIFEPILOT_JWT_SECRET` through environment variables. Treat logs and AI tool inputs as potentially sensitive; avoid recording secrets or personal content unnecessarily.
