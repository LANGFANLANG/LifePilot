# LifePilot MVP

LifePilot is a Spring Boot MVP for personal task, note, and AI-assisted life planning workflows.

## Prerequisites

- Java 21
- Gradle, or the included Gradle wrapper
- Docker Desktop with the Linux engine running
- OpenAI API key for real chat calls

## Local Configuration

The local profile is enabled by default. Local API authentication is disabled by default through:

```yaml
lifepilot:
  security:
    auth-enabled: false
```

Set your OpenAI API key before starting the app:

```powershell
$env:OPENAI_API_KEY = "your-openai-api-key"
```

For JWT-enabled local testing, provide a secret with at least 32 bytes and enable auth in configuration:

```powershell
$env:LIFEPILOT_JWT_SECRET = "change-me-change-me-change-me-change-me"
```

## Start Dependencies

Start Postgres with pgvector and Redis:

```powershell
docker compose up -d postgres redis
```

Check container status:

```powershell
docker compose ps
```

## Start The App

Run the Spring Boot app:

```powershell
.\gradlew.bat bootRun
```

The API is available at:

```text
http://localhost:8080
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Test Commands

Run the automated test suite:

```powershell
.\gradlew.bat test
```

Generate Javadoc:

```powershell
.\gradlew.bat javadoc
```

Run the local smoke test after the app is started:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

To target a different host:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1 -BaseUrl "http://localhost:8080"
```

## MVP Endpoints

- `GET /actuator/health`
- `POST /api/todos`
- `GET /api/todos`
- `POST /api/notes`
- `GET /api/notes`
- `GET /api/notes/{id}`
- `POST /api/chat`

## Troubleshooting

If tests that use the local database fail with a connection error, confirm Docker Desktop is running and the Linux engine is available:

```powershell
docker compose up -d postgres redis
```

If PowerShell refuses to run the smoke test because of execution policy, use the `-ExecutionPolicy Bypass` command shown above. It applies only to that PowerShell process.
