# Chat History User Scope Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build persisted AI chat history that is listed in the chat UI, survives refresh, and is isolated by logged-in user.

**Architecture:** Add `user_id` ownership to `conversations`, expose ownership-aware chat history APIs, and update the Vue chat screen to load and switch persisted conversations. The database remains the source of truth; browser state only tracks the currently selected conversation.

**Tech Stack:** Java 21, Spring Boot, MyBatis Plus, Flyway, Sa-Token, JUnit 5, Mockito, MockMvc, Vue 3, Vite.

## Global Constraints

- Only AI conversations get user isolation in this feature.
- Todo, note, review, reminder, and plan preview ownership remain out of scope.
- Do not use browser storage as the source of truth for chat history.
- Keep `POST /api/chat` response shape compatible with existing frontend calls.
- Existing dirty files in the workspace are unrelated and must not be reverted.

---

## File Structure

- Create `src/main/resources/db/migration/V8__add_user_scope_to_conversations.sql`: adds nullable `user_id` and index.
- Modify `src/main/java/com/lifepilot/domain/Conversation.java`: stores `userId` and creates user-owned conversations.
- Modify `src/main/java/com/lifepilot/repository/ConversationRepository.java`: adds user-scoped lookup and list queries.
- Modify `src/main/java/com/lifepilot/memory/ChatMemoryService.java`: adds user-scoped create/list/load/append methods.
- Modify `src/main/java/com/lifepilot/agent/dto/AgentRequest.java`: includes `userId`.
- Modify `src/main/java/com/lifepilot/agent/AgentService.java`: resolves conversations through user ownership.
- Modify `src/main/java/com/lifepilot/controller/ChatController.java`: derives current user id and exposes history endpoints.
- Modify `src/test/java/com/lifepilot/memory/ChatMemoryServiceTest.java`: covers user ownership behavior.
- Modify `src/test/java/com/lifepilot/agent/AgentServiceTest.java`: covers passing user id through chat flow.
- Modify `src/test/java/com/lifepilot/controller/ChatControllerTest.java`: covers new endpoints and user id in chat requests.
- Modify `frontend/src/api/chat.js`: adds list and message loading functions.
- Modify `frontend/src/views/ChatView.vue`: adds conversation list, loading, selection, refresh restore.
- Modify `frontend/src/styles/main.css`: styles the chat history column responsively.

---

### Task 1: Database And Domain Ownership

**Files:**
- Create: `src/main/resources/db/migration/V8__add_user_scope_to_conversations.sql`
- Modify: `src/main/java/com/lifepilot/domain/Conversation.java`
- Modify: `src/main/java/com/lifepilot/repository/ConversationRepository.java`
- Test: `src/test/java/com/lifepilot/memory/ChatMemoryServiceTest.java`

**Interfaces:**
- Produces: `Conversation.create(UUID userId, String title) : Conversation`
- Produces: `Conversation.getUserId() : UUID`
- Produces: `ConversationRepository.findByUserIdOrderByUpdatedAtDesc(UUID userId) : List<Conversation>`
- Produces: `ConversationRepository.findByIdAndUserId(UUID id, UUID userId) : Optional<Conversation>`

- [ ] **Step 1: Write failing repository/service-facing tests**

Add tests to `ChatMemoryServiceTest`:

```java
@Test
void createsConversationForUser() {
    UUID userId = UUID.randomUUID();
    when(conversationRepository.save(any(Conversation.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    ConversationView conversation = chatMemoryService.createConversation(userId, "Project planning");

    ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
    verify(conversationRepository).save(captor.capture());
    assertThat(conversation.id()).isNotNull();
    assertThat(captor.getValue().getUserId()).isEqualTo(userId);
}

@Test
void listsConversationsForUserByRepositoryOrder() {
    UUID userId = UUID.randomUUID();
    Conversation first = Conversation.create(userId, "First");
    Conversation second = Conversation.create(userId, "Second");
    when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId))
            .thenReturn(List.of(second, first));

    List<ConversationView> conversations = chatMemoryService.listConversations(userId);

    assertThat(conversations).extracting(ConversationView::title)
            .containsExactly("Second", "First");
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat test --tests com.lifepilot.memory.ChatMemoryServiceTest`

Expected: compile failure because `createConversation(UUID, String)` and `listConversations(UUID)` do not exist.

- [ ] **Step 3: Implement migration, domain, and repository methods**

Create `V8__add_user_scope_to_conversations.sql`:

```sql
ALTER TABLE conversations
    ADD COLUMN user_id UUID REFERENCES user_accounts(id);

CREATE INDEX idx_conversations_user_updated
    ON conversations(user_id, updated_at DESC);
```

Update `Conversation`:

```java
private UUID userId;

private Conversation(UUID id, UUID userId, String title, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
}

public static Conversation create(UUID userId, String title) {
    OffsetDateTime now = OffsetDateTime.now();
    return new Conversation(UUID.randomUUID(), userId, title, now, now);
}

public static Conversation create(String title) {
    return create(null, title);
}

public UUID getUserId() {
    return userId;
}
```

Update `ConversationRepository`:

```java
default List<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId) {
    return selectList(Wrappers.lambdaQuery(Conversation.class)
            .eq(Conversation::getUserId, userId)
            .orderByDesc(Conversation::getUpdatedAt));
}

default Optional<Conversation> findByIdAndUserId(UUID id, UUID userId) {
    return Optional.ofNullable(selectOne(Wrappers.lambdaQuery(Conversation.class)
            .eq(Conversation::getId, id)
            .eq(Conversation::getUserId, userId)));
}
```

- [ ] **Step 4: Run task tests**

Run: `.\gradlew.bat test --tests com.lifepilot.memory.ChatMemoryServiceTest`

Expected: tests still fail because service methods are not implemented until Task 2, or pass if service compatibility methods are added immediately.

---

### Task 2: User-Scoped Chat Memory And Agent Flow

**Files:**
- Modify: `src/main/java/com/lifepilot/memory/ChatMemoryService.java`
- Modify: `src/main/java/com/lifepilot/agent/dto/AgentRequest.java`
- Modify: `src/main/java/com/lifepilot/agent/AgentService.java`
- Test: `src/test/java/com/lifepilot/memory/ChatMemoryServiceTest.java`
- Test: `src/test/java/com/lifepilot/agent/AgentServiceTest.java`

**Interfaces:**
- Consumes: `Conversation.create(UUID userId, String title)`
- Consumes: `ConversationRepository.findByIdAndUserId(UUID id, UUID userId)`
- Produces: `ChatMemoryService.createConversation(UUID userId, String title) : ConversationView`
- Produces: `ChatMemoryService.listConversations(UUID userId) : List<ConversationView>`
- Produces: `ChatMemoryService.loadMessages(UUID userId, UUID conversationId) : List<MessageView>`
- Produces: `ChatMemoryService.appendMessage(UUID userId, UUID conversationId, ChatRole role, String content) : MessageView`
- Produces: `AgentRequest(UUID userId, UUID conversationId, String message)`

- [ ] **Step 1: Write failing service ownership tests**

Add tests to `ChatMemoryServiceTest`:

```java
@Test
void loadsMessagesOnlyForOwningUser() {
    UUID userId = UUID.randomUUID();
    Conversation conversation = Conversation.create(userId, "Project planning");
    ChatMessage userMessage = conversation.addMessage(ChatRole.USER, "Create a task");
    when(conversationRepository.findByIdAndUserId(conversation.getId(), userId))
            .thenReturn(Optional.of(conversation));
    when(chatMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversation.getId()))
            .thenReturn(List.of(userMessage));

    List<MessageView> messages = chatMemoryService.loadMessages(userId, conversation.getId());

    assertThat(messages).extracting(MessageView::content).containsExactly("Create a task");
}

@Test
void rejectsAppendingMessageToAnotherUsersConversation() {
    UUID userId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    when(conversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> chatMemoryService.appendMessage(userId, conversationId, ChatRole.USER, "Nope"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("conversation not found");
}
```

Add or update `AgentServiceTest` expectation so `AgentRequest` includes `userId`:

```java
UUID userId = UUID.randomUUID();
AgentResponse response = agentService.chat(new AgentRequest(userId, null, "Create a task"));
```

- [ ] **Step 2: Run tests to verify failure**

Run:

```powershell
.\gradlew.bat test --tests com.lifepilot.memory.ChatMemoryServiceTest --tests com.lifepilot.agent.AgentServiceTest
```

Expected: compile failures for missing methods and record constructor.

- [ ] **Step 3: Implement service and agent changes**

Update `ChatMemoryService`:

```java
@Transactional
public ConversationView createConversation(UUID userId, String title) {
    return ConversationView.from(conversationRepository.save(Conversation.create(userId, title)));
}

@Transactional(readOnly = true)
public List<ConversationView> listConversations(UUID userId) {
    return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
            .map(ConversationView::from)
            .toList();
}

@Transactional
public MessageView appendMessage(UUID userId, UUID conversationId, ChatRole role, String content) {
    Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
    ChatMessage message = conversation.addMessage(role, content);
    conversationRepository.save(conversation);
    return MessageView.from(chatMessageRepository.save(message));
}

@Transactional(readOnly = true)
public List<MessageView> loadMessages(UUID userId, UUID conversationId) {
    conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
    return loadRecentMessages(conversationId);
}
```

Keep old service methods as compatibility wrappers where existing tests or callers still use them.

Update `AgentRequest`:

```java
public record AgentRequest(UUID userId, UUID conversationId, String message) {
}
```

Update `AgentService`:

```java
private UUID resolveConversationId(AgentRequest request) {
    if (request.conversationId() != null) {
        return chatMemoryService.requireConversation(request.userId(), request.conversationId()).id();
    }
    ConversationView conversation = chatMemoryService.createConversation(request.userId(), request.message());
    return conversation.id();
}
```

If using `requireConversation`, add it to `ChatMemoryService`:

```java
@Transactional(readOnly = true)
public ConversationView requireConversation(UUID userId, UUID conversationId) {
    return conversationRepository.findByIdAndUserId(conversationId, userId)
            .map(ConversationView::from)
            .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
}
```

- [ ] **Step 4: Run task tests**

Run:

```powershell
.\gradlew.bat test --tests com.lifepilot.memory.ChatMemoryServiceTest --tests com.lifepilot.agent.AgentServiceTest
```

Expected: PASS.

---

### Task 3: Chat History HTTP API

**Files:**
- Modify: `src/main/java/com/lifepilot/controller/ChatController.java`
- Test: `src/test/java/com/lifepilot/controller/ChatControllerTest.java`

**Interfaces:**
- Consumes: `AgentRequest(UUID userId, UUID conversationId, String message)`
- Consumes: `ChatMemoryService.listConversations(UUID userId)`
- Consumes: `ChatMemoryService.loadMessages(UUID userId, UUID conversationId)`
- Produces: `GET /api/chat/conversations`
- Produces: `GET /api/chat/conversations/{conversationId}/messages`

- [ ] **Step 1: Write failing controller tests**

Update `ChatControllerTest` to mock `ChatMemoryService` and static Sa-Token login id. Use `MockedStatic<StpUtil>`:

```java
@MockBean
private ChatMemoryService chatMemoryService;

@Test
void listsCurrentUsersConversations() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    when(chatMemoryService.listConversations(userId)).thenReturn(List.of(
            new ConversationView(conversationId, "Project planning", OffsetDateTime.parse("2026-08-11T10:00:00+08:00"), OffsetDateTime.parse("2026-08-11T10:05:00+08:00"))
    ));

    try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
        stp.when(StpUtil::getLoginIdAsString).thenReturn(userId.toString());

        mockMvc.perform(get("/api/chat/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(conversationId.toString()))
                .andExpect(jsonPath("$.data[0].title").value("Project planning"));
    }
}

@Test
void loadsCurrentUsersConversationMessages() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();
    when(chatMemoryService.loadMessages(userId, conversationId)).thenReturn(List.of(
            new MessageView(messageId, conversationId, ChatRole.USER, "Hello", OffsetDateTime.parse("2026-08-11T10:00:00+08:00"))
    ));

    try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
        stp.when(StpUtil::getLoginIdAsString).thenReturn(userId.toString());

        mockMvc.perform(get("/api/chat/conversations/{conversationId}/messages", conversationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("Hello"));
    }
}
```

Update existing chat test to verify the controller passes `userId`:

```java
ArgumentCaptor<AgentRequest> captor = ArgumentCaptor.forClass(AgentRequest.class);
verify(agentService).chat(captor.capture());
assertThat(captor.getValue().userId()).isEqualTo(userId);
```

- [ ] **Step 2: Run controller tests to verify failure**

Run: `.\gradlew.bat test --tests com.lifepilot.controller.ChatControllerTest`

Expected: compile failure for missing endpoints or missing `ChatMemoryService` injection.

- [ ] **Step 3: Implement controller endpoints**

Update `ChatController`:

```java
private final ChatMemoryService chatMemoryService;

public ChatController(AgentService agentService, ChatMemoryService chatMemoryService) {
    this.agentService = agentService;
    this.chatMemoryService = chatMemoryService;
}

@GetMapping("/conversations")
public Result<List<ConversationView>> listConversations() {
    return Result.success(chatMemoryService.listConversations(currentUserId()));
}

@GetMapping("/conversations/{conversationId}/messages")
public Result<List<MessageView>> listMessages(@PathVariable UUID conversationId) {
    return Result.success(chatMemoryService.loadMessages(currentUserId(), conversationId));
}

@PostMapping
public Result<AgentResponse> chat(@Valid @RequestBody ChatRequest request) {
    return Result.success(agentService.chat(new AgentRequest(currentUserId(), request.conversationId(), request.message())));
}

private UUID currentUserId() {
    return UUID.fromString(StpUtil.getLoginIdAsString());
}
```

Add imports for `GetMapping`, `PathVariable`, `StpUtil`, `ConversationView`, `MessageView`, `ChatMemoryService`, `List`, and `UUID`.

- [ ] **Step 4: Run controller tests**

Run: `.\gradlew.bat test --tests com.lifepilot.controller.ChatControllerTest`

Expected: PASS.

---

### Task 4: Vue Chat History UI

**Files:**
- Modify: `frontend/src/api/chat.js`
- Modify: `frontend/src/views/ChatView.vue`
- Modify: `frontend/src/styles/main.css`

**Interfaces:**
- Consumes: `GET /api/chat/conversations`
- Consumes: `GET /api/chat/conversations/{conversationId}/messages`
- Consumes: `POST /api/chat`
- Produces: `listChatConversations() : Promise<Array>`
- Produces: `listChatMessages(conversationId: string) : Promise<Array>`

- [ ] **Step 1: Add frontend API functions**

Update `frontend/src/api/chat.js`:

```js
export function listChatConversations() {
  return request('/api/chat/conversations')
}

export function listChatMessages(conversationId) {
  return request(`/api/chat/conversations/${conversationId}/messages`)
}
```

- [ ] **Step 2: Update ChatView state and lifecycle**

In `ChatView.vue`, import `onMounted` and the new API functions:

```js
import { computed, nextTick, onMounted, ref } from 'vue'
import { listChatConversations, listChatMessages, sendChatMessage } from '../api/chat'
```

Add state:

```js
const conversations = ref([])
const loadingConversations = ref(false)
const loadingMessages = ref(false)
```

Add functions:

```js
function normalizeMessage(message) {
  return {
    id: message.id,
    role: String(message.role || '').toLowerCase(),
    content: message.content,
    createdAt: message.createdAt,
  }
}

function formatConversationTime(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}

async function loadConversations(selectLatest = true) {
  loadingConversations.value = true
  error.value = ''
  try {
    conversations.value = await listChatConversations()
    if (selectLatest && conversations.value.length) {
      await selectConversation(conversations.value[0].id)
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loadingConversations.value = false
  }
}

async function selectConversation(id) {
  if (!id || loadingMessages.value) return
  conversationId.value = id
  loadingMessages.value = true
  error.value = ''
  try {
    const loaded = await listChatMessages(id)
    messages.value = loaded.map(normalizeMessage).filter((message) => ['user', 'assistant'].includes(message.role))
    await scrollToBottom()
  } catch (e) {
    error.value = e.message
    conversationId.value = null
    messages.value = []
    await loadConversations(false)
  } finally {
    loadingMessages.value = false
  }
}

function startNewConversation() {
  conversationId.value = null
  messages.value = []
  error.value = ''
}

onMounted(() => {
  loadConversations()
})
```

- [ ] **Step 3: Update send flow to refresh conversation list**

After successful `sendChatMessage`:

```js
const wasNewConversation = !conversationId.value
const reply = await sendChatMessage(conversationId.value, text)
conversationId.value = reply.conversationId
const plans = await loadPlanActions(reply.actions || [])
messages.value.push({ role: 'assistant', content: reply.content, plans })
if (wasNewConversation) {
  await loadConversations(false)
} else {
  loadConversations(false)
}
```

- [ ] **Step 4: Add template conversation column**

Wrap existing chat area with:

```vue
<div class="chat-workspace">
  <aside class="chat-history">
    <div class="chat-history-head">
      <span>会话记录</span>
      <button class="btn btn-ghost" type="button" @click="startNewConversation">新对话</button>
    </div>
    <div v-if="loadingConversations" class="chat-history-empty">正在读取会话...</div>
    <div v-else-if="!conversations.length" class="chat-history-empty">还没有会话</div>
    <button
      v-for="conversation in conversations"
      v-else
      :key="conversation.id"
      type="button"
      class="chat-history-item"
      :class="{ active: conversation.id === conversationId }"
      @click="selectConversation(conversation.id)"
    >
      <span>{{ conversation.title }}</span>
      <time>{{ formatConversationTime(conversation.updatedAt) }}</time>
    </button>
  </aside>

  <div class="chat-shell">...</div>
</div>
```

Preserve the existing chat log, message rendering, plan preview controls, and composer inside the right-side `chat-shell`.

- [ ] **Step 5: Add CSS**

Add CSS near the chat section:

```css
.chat-workspace {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 22px;
  height: calc(100vh - 118px);
  min-height: 480px;
}

.chat-workspace .chat-shell {
  height: 100%;
  min-height: 0;
}

.chat-history {
  border-right: 1px solid var(--hairline);
  padding-right: 16px;
  overflow-y: auto;
}

.chat-history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.chat-history-head span {
  font-size: 13px;
  color: var(--muted);
  font-weight: 650;
}

.chat-history-head .btn {
  padding: 6px 9px;
  font-size: 12px;
}

.chat-history-item {
  width: 100%;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--ink-soft);
  text-align: left;
  padding: 10px 11px;
  margin-bottom: 6px;
}

.chat-history-item:hover {
  background: rgba(230, 223, 212, 0.55);
}

.chat-history-item.active {
  background: var(--ink);
  color: var(--bg);
}

.chat-history-item span,
.chat-history-item time {
  display: block;
}

.chat-history-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13.5px;
}

.chat-history-item time {
  margin-top: 4px;
  font-family: var(--font-display);
  font-size: 11.5px;
  color: var(--muted);
}

.chat-history-item.active time {
  color: rgba(246, 243, 238, 0.62);
}

.chat-history-empty {
  color: var(--muted);
  font-size: 13px;
  padding: 18px 8px;
}
```

Add responsive rule inside the existing `@media (max-width: 860px)` block:

```css
.chat-workspace {
  grid-template-columns: 1fr;
  height: calc(100vh - 200px);
}

.chat-history {
  border-right: 0;
  border-bottom: 1px solid var(--hairline);
  padding-right: 0;
  padding-bottom: 10px;
  max-height: 160px;
}
```

- [ ] **Step 6: Build frontend**

Run: `npm --prefix frontend run build`

Expected: PASS.

---

### Task 5: Full Verification And Commit

**Files:**
- All files from Tasks 1-4.

**Interfaces:**
- Consumes: all implemented backend and frontend behavior.
- Produces: one verified implementation commit.

- [ ] **Step 1: Run backend test subset**

Run:

```powershell
.\gradlew.bat test --tests com.lifepilot.memory.ChatMemoryServiceTest --tests com.lifepilot.agent.AgentServiceTest --tests com.lifepilot.controller.ChatControllerTest
```

Expected: PASS.

- [ ] **Step 2: Run full backend tests**

Run: `.\gradlew.bat test`

Expected: PASS.

- [ ] **Step 3: Run frontend build**

Run: `npm --prefix frontend run build`

Expected: PASS.

- [ ] **Step 4: Review git diff**

Run: `git diff --stat`

Expected: only chat history implementation, plan doc, and migration are included. Pre-existing unrelated dirty files remain untouched unless already dirty before this work.

- [ ] **Step 5: Commit implementation**

Run:

```powershell
git add src/main/resources/db/migration/V8__add_user_scope_to_conversations.sql src/main/java/com/lifepilot/domain/Conversation.java src/main/java/com/lifepilot/repository/ConversationRepository.java src/main/java/com/lifepilot/memory/ChatMemoryService.java src/main/java/com/lifepilot/agent/dto/AgentRequest.java src/main/java/com/lifepilot/agent/AgentService.java src/main/java/com/lifepilot/controller/ChatController.java src/test/java/com/lifepilot/memory/ChatMemoryServiceTest.java src/test/java/com/lifepilot/agent/AgentServiceTest.java src/test/java/com/lifepilot/controller/ChatControllerTest.java frontend/src/api/chat.js frontend/src/views/ChatView.vue frontend/src/styles/main.css docs/superpowers/plans/2026-08-11-chat-history-user-scope.md
git commit -m "feat: add user-scoped chat history"
```

Expected: commit succeeds.
