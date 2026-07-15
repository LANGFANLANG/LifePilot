# LifePilot Personal Execution Assistant Design

## Product Goal

LifePilot should become a personal AI execution assistant that is useful every day. Its core loop is:

```text
Capture a goal → break it into tasks → plan today → send reminders → record outcomes → generate reviews
```

The project remains single-user-first. Multi-user administration, billing, high concurrency, and broad integrations are out of scope until the personal workflow is proven useful.

## User Experience

The home page becomes a “Today Dashboard” with five areas:

1. **Today’s Focus** — at most three high-impact tasks.
2. **Timeline** — tasks ordered by planned start time.
3. **Inbox** — quick capture for unprocessed ideas and obligations.
4. **Progress** — completed, pending, and overdue counts.
5. **AI Guidance** — a concrete recommendation for the next action.

When the user enters a goal such as “launch my personal blog before month-end,” the AI asks for missing constraints, proposes a task breakdown, and waits for confirmation before saving tasks.

## Domain Model

Extend todos with priority, category, estimated duration, planned start time, reminder time, completion time, parent task, source, and postponement count. An inbox item remains unplanned until it is converted into a task, note, or discarded.

Daily reviews store completed work, unfinished work, newly added tasks, reflections, and tomorrow’s plan. Weekly reviews aggregate daily reviews into achievements, blockers, delayed work, and next-week priorities.

## Data Flow and Scheduling

A planning service ranks tasks using due date, priority, age, and estimated effort. The AI may explain or adjust a plan, but deterministic application rules remain the source of truth. A Spring scheduler periodically finds due reminders and records delivery attempts. The first version uses in-app notifications; external channels can be added behind a notification interface.

Review generation reads task history and execution logs, produces a draft, and lets the user edit before saving. AI failures must not block task management, reminders, or manual reviews.

## Error Handling and Safety

AI-created task plans require confirmation. Duplicate reminder delivery is prevented with a delivery record and unique key. Invalid dates, missing tasks, unavailable AI service, and notification failures return distinct user-facing errors. Secrets and private task content must not be written to request logs.

## Testing Strategy

Use unit tests for ranking, task breakdown confirmation, recurrence, and review aggregation. Use repository tests for scheduling queries and reminder idempotency. Mock the AI and notification gateways. Add MockMvc tests for new APIs and an end-to-end flow covering goal capture through daily review.

## Delivery Phases

1. Extend task fields and add edit/delete APIs.
2. Build the Today Dashboard and deterministic task ranking.
3. Add AI goal decomposition with preview and confirmation.
4. Add scheduled reminders and in-app notification history.
5. Add editable daily and weekly review generation.
6. Add calendar view, smarter scheduling, and optional external notification channels.

The first usable milestone is complete when a user can capture a goal, confirm its task breakdown, see an ordered daily plan, receive a reminder, complete work, and generate a daily review.
