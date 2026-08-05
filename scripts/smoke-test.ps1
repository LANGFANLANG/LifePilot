param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Stop"

function Invoke-LifePilotGet {
    param(
        [string]$Path
    )

    Invoke-RestMethod -Method Get -Uri "$BaseUrl$Path"
}

function Invoke-LifePilotPost {
    param(
        [string]$Path,
        [hashtable]$Body
    )

    Invoke-RestMethod `
        -Method Post `
        -Uri "$BaseUrl$Path" `
        -ContentType "application/json" `
        -Body ($Body | ConvertTo-Json -Depth 8)
}

function Assert-ApiSuccess {
    param(
        [object]$Response,
        [string]$Step
    )

    if ($Response.success -ne $true) {
        throw "$Step failed: API result was not successful"
    }
}

Write-Host "Running LifePilot smoke test against $BaseUrl"

$health = Invoke-LifePilotGet -Path "/actuator/health"
if ($health.status -ne "UP") {
    throw "health failed: expected UP but got $($health.status)"
}
Write-Host "[OK] health"

$createdTodo = Invoke-LifePilotPost -Path "/api/todos" -Body @{
    title = "Smoke test todo"
    description = "Created by scripts/smoke-test.ps1"
    dueAt = $null
}
Assert-ApiSuccess -Response $createdTodo -Step "create todo"
Write-Host "[OK] create todo"

$todos = Invoke-LifePilotGet -Path "/api/todos"
Assert-ApiSuccess -Response $todos -Step "list todos"
Write-Host "[OK] list todos"

$createdNote = Invoke-LifePilotPost -Path "/api/notes" -Body @{
    title = "Smoke test note"
    content = "Created by scripts/smoke-test.ps1"
}
Assert-ApiSuccess -Response $createdNote -Step "create note"
Write-Host "[OK] create note"

$notes = Invoke-LifePilotGet -Path "/api/notes"
Assert-ApiSuccess -Response $notes -Step "list notes"
Write-Host "[OK] list notes"

$chat = Invoke-LifePilotPost -Path "/api/chat" -Body @{
    conversationId = $null
    message = "Say hello from the smoke test."
}
Assert-ApiSuccess -Response $chat -Step "send chat message"
Write-Host "[OK] send chat message"

Write-Host "LifePilot smoke test passed."
