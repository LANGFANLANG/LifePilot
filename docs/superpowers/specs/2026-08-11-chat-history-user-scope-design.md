# Chat History With User Scope Design

## Context

LifePilot already persists AI conversations through `conversations` and `chat_messages`. The current Vue chat page keeps the visible message list only in component memory, so a browser refresh loses the displayed conversation even though messages have been written to the database. The user wants a conversation record area in the AI assistant screen and each conversation's content to survive refreshes.

Authentication also exists through `user_accounts` and Sa-Token. This feature must isolate AI conversations per logged-in user. Todo and note ownership are intentionally out of scope for this change.

## Goals

- Show a persisted conversation list in the AI assistant page, matching the empty left column area in the provided screenshot.
- Let a user open any of their own conversations and restore all messages after refresh.
- Bind newly created conversations to the current logged-in user.
- Reject attempts to read or continue another user's conversation.
- Keep the existing `POST /api/chat` behavior for the frontend, while adding the ownership checks and enough read APIs for history.

## Non-Goals

- Do not add user isolation to todos, notes, reviews, reminders, or plan previews in this feature.
- Do not add conversation rename, delete, search, or pinning yet.
- Do not change the AI client prompt or tool behavior except where conversation ownership is required.
- Do not replace the current chat persistence model with browser storage.

## Recommended Approach

Use backend persistence as the source of truth. Add `user_id` to `conversations`, expose authenticated read APIs for conversations and messages, and update the Vue chat view to load that data.

This keeps refresh recovery reliable, lets the server enforce ownership, and reuses the existing chat memory tables instead of creating a parallel client-side session model.

## Data Model

Add a Flyway migration that extends `conversations`:

- `user_id UUID NULL REFERENCES user_accounts(id)`
- index on `(user_id, updated_at DESC)`

The column starts nullable so existing local conversation rows can survive migration. Application-created conversations after this feature must always set `user_id`.

Ownership rules:

- When auth is enabled and a request is logged in, new conversations use the current login id as `user_id`.
- Read and continuation APIs require the target conversation's `user_id` to match the current login id.
- Legacy conversations with `user_id IS NULL` should not be shown in the authenticated conversation list. This avoids accidentally exposing old rows to every account.

## Backend API

Keep:

- `POST /api/chat`

Change `POST /api/chat` internally:

- If `conversationId` is absent, create a conversation for the current user.
- If `conversationId` is present, verify that it belongs to the current user before appending a message.
- Return the same `AgentResponse` shape so existing callers keep working.

Add:

- `GET /api/chat/conversations`
  - Returns `List<ConversationView>` for the current user, ordered by `updatedAt` descending.

- `GET /api/chat/conversations/{conversationId}/messages`
  - Verifies ownership.
  - Returns `List<MessageView>` ordered by `createdAt` ascending.

Possible later endpoint, not part of this feature:

- `DELETE /api/chat/conversations/{conversationId}`

## Backend Components

`Conversation`

- Add `userId`.
- Add a factory that accepts `userId` and title.
- Keep title generation as the first user message, matching current behavior.

`ConversationRepository`

- Add `findByUserIdOrderByUpdatedAtDesc(UUID userId)`.
- Add `findByIdAndUserId(UUID id, UUID userId)`.

`ChatMemoryService`

- Add `createConversation(UUID userId, String title)`.
- Add `listConversations(UUID userId)`.
- Add `loadMessages(UUID userId, UUID conversationId)`.
- Add ownership-aware append behavior, either by loading conversation with `findByIdAndUserId` or by a dedicated guard method.

`AgentService`

- Accept the current user id for chat requests.
- Resolve conversation id using ownership-aware memory methods.
- Keep the existing order: append user message, load history, call AI, append assistant reply, record execution log.

`ChatController`

- Derive current user id from Sa-Token login state.
- Add the two read endpoints.
- Continue returning `Result<T>`.

## Frontend Design

Update `frontend/src/views/ChatView.vue` into a two-column chat work surface:

- Left column: conversation history.
- Right column: existing chat log and composer.

Conversation list behavior:

- On page mount, call `GET /api/chat/conversations`.
- Render each conversation with title and last updated time.
- Select the most recent conversation by default and load its messages.
- Provide a `New chat` button that clears the selected conversation and message list.
- After sending the first message in a new conversation, use the returned `conversationId`, reload or locally prepend the conversation list, and mark it active.
- On refresh, the list loads again and the newest conversation restores automatically.

Message rendering:

- Map backend `ChatRole.USER` to the existing user bubble.
- Map backend `ChatRole.ASSISTANT` to the existing assistant markdown bubble.
- Ignore or render `SYSTEM` and `TOOL` defensively if they ever appear, but current normal UI will primarily show user and assistant messages.

Responsive behavior:

- Desktop: history column remains visible at the left of the chat page content.
- Mobile: stack the conversation list above the chat log or make it a compact horizontal selector.

## Error Handling

- If conversation list loading fails, show an inline error and keep the composer disabled until the user retries or starts a new conversation.
- If a selected conversation is missing or forbidden, clear the active selection, show a friendly error, and reload the list.
- If sending fails, keep the optimistic user message visible with an assistant error bubble, matching current behavior.

## Testing

Backend tests:

- `ChatMemoryServiceTest`
  - Creates a conversation with a user id.
  - Lists only that user's conversations.
  - Loads messages only for the owning user.
  - Rejects appending to a conversation owned by another user.

- `ChatControllerTest`
  - Covers conversation list endpoint.
  - Covers message list endpoint.
  - Covers `POST /api/chat` passing the current user id into `AgentService`.

- Repository or integration test, if existing test setup supports it:
  - Verifies ordering by `updatedAt DESC`.

Frontend verification:

- Load `/chat`, confirm conversation list appears.
- Send a new message, refresh, confirm the conversation and messages are restored.
- Start a new conversation, confirm the previous one remains in the list.

## Implementation Notes

- Keep API response DTOs as records and do not expose entities directly.
- Add concise Javadoc to new public methods, matching repository guidelines.
- Avoid broad auth/data ownership refactors in this feature.
- Existing dirty files in the workspace are unrelated and should not be reverted.
