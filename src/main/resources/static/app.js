const $ = (selector) => document.querySelector(selector);
const state = { conversationId: null, todos: [], notes: [] };

const api = async (url, options = {}) => {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json", ...options.headers },
        ...options
    });
    let body;
    try {
        body = await response.json();
    } catch {
        throw new Error("服务返回了无法识别的内容");
    }
    if (!response.ok || !body.success) {
        throw new Error(body.message || "服务暂时没有响应，请稍后再试");
    }
    return body.data;
};

const escapeHtml = (value = "") => String(value).replace(/[&<>'"]/g, (char) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "'": "&#39;",
    "\"": "&quot;"
})[char]);

const renderInlineMarkdown = (value = "") => escapeHtml(value)
    .replace(/`([^`]+)`/g, "<code>$1</code>")
    .replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>")
    .replace(/\*([^*]+)\*/g, "<em>$1</em>");

const renderMarkdown = (value = "") => {
    const lines = String(value).replace(/\r\n/g, "\n").split("\n");
    const blocks = [];
    let paragraph = [];
    let list = [];
    let listType = null;
    let code = [];
    let inCodeBlock = false;

    const flushParagraph = () => {
        if (!paragraph.length) return;
        blocks.push(`<p>${renderInlineMarkdown(paragraph.join(" "))}</p>`);
        paragraph = [];
    };
    const flushList = () => {
        if (!list.length) return;
        const tag = listType === "ol" ? "ol" : "ul";
        blocks.push(`<${tag}>${list.map((item) => `<li>${renderInlineMarkdown(item)}</li>`).join("")}</${tag}>`);
        list = [];
        listType = null;
    };
    const flushCode = () => {
        blocks.push(`<pre><code>${escapeHtml(code.join("\n"))}</code></pre>`);
        code = [];
    };

    for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                flushCode();
                inCodeBlock = false;
            } else {
                flushParagraph();
                flushList();
                inCodeBlock = true;
            }
            continue;
        }
        if (inCodeBlock) {
            code.push(line);
            continue;
        }
        if (!trimmed) {
            flushParagraph();
            flushList();
            continue;
        }

        const heading = trimmed.match(/^(#{1,3})\s+(.+)$/);
        if (heading) {
            flushParagraph();
            flushList();
            const level = heading[1].length + 2;
            blocks.push(`<h${level}>${renderInlineMarkdown(heading[2])}</h${level}>`);
            continue;
        }

        const unordered = trimmed.match(/^[-*]\s+(.+)$/);
        const ordered = trimmed.match(/^\d+\.\s+(.+)$/);
        if (unordered || ordered) {
            flushParagraph();
            const nextType = ordered ? "ol" : "ul";
            if (listType && listType !== nextType) flushList();
            listType = nextType;
            list.push((unordered || ordered)[1]);
            continue;
        }

        flushList();
        paragraph.push(trimmed);
    }

    flushParagraph();
    flushList();
    if (inCodeBlock || code.length) flushCode();
    return blocks.join("");
};

const showToast = (message, error = false) => {
    const toast = $("#toast");
    toast.textContent = message;
    toast.className = `toast show${error ? " error" : ""}`;
    clearTimeout(showToast.timer);
    showToast.timer = setTimeout(() => toast.className = "toast", 2600);
};

const toApiDate = (value) => value ? new Date(value).toISOString() : null;
const toInputDate = (value) => value ? new Date(value).toISOString().slice(0, 16) : "";
const friendlyDate = (value) => value ? new Intl.DateTimeFormat("zh-CN", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" }).format(new Date(value)) : "";
const priorityText = (priority) => ({ HIGH: "高", MEDIUM: "中", LOW: "低" })[priority] || "中";

function readTodoForm(prefix = "todo") {
    const estimated = $(`#${prefix}Estimated`).value;
    return {
        title: $(`#${prefix}Title`).value.trim(),
        description: $(`#${prefix}Description`).value.trim() || null,
        dueAt: null,
        priority: $(`#${prefix}Priority`).value || "MEDIUM",
        category: $(`#${prefix}Category`).value.trim() || null,
        estimatedMinutes: estimated === "" ? null : Number(estimated),
        plannedStartAt: toApiDate($(`#${prefix}PlannedStart`).value),
        reminderAt: toApiDate($(`#${prefix}Reminder`).value),
        parentTodoId: null,
        source: "manual"
    };
}

function renderTodos() {
    const active = state.todos.filter((todo) => todo.status !== "COMPLETED");
    const done = state.todos.filter((todo) => todo.status === "COMPLETED");
    const planned = active.filter((todo) => todo.plannedStartAt);
    $("#todoCount").textContent = active.length;
    $("#doneCount").textContent = done.length;
    $("#plannedCount").textContent = planned.length;

    $("#todoList").innerHTML = state.todos.length ? state.todos.map((todo) => {
        const meta = [
            `<span class="pill priority-${escapeHtml((todo.priority || "MEDIUM").toLowerCase())}">${priorityText(todo.priority)}优先级</span>`,
            todo.category ? `<span class="pill">${escapeHtml(todo.category)}</span>` : "",
            todo.estimatedMinutes != null ? `<span class="pill">${todo.estimatedMinutes} 分钟</span>` : "",
            todo.plannedStartAt ? `<span class="pill">计划 ${friendlyDate(todo.plannedStartAt)}</span>` : "",
            todo.reminderAt ? `<span class="pill">提醒 ${friendlyDate(todo.reminderAt)}</span>` : ""
        ].filter(Boolean).join("");

        return `
            <article class="todo-item ${todo.status === "COMPLETED" ? "done" : ""}">
                <div class="todo-main">
                    <h3>${escapeHtml(todo.title)}</h3>
                    ${todo.description ? `<p>${escapeHtml(todo.description)}</p>` : ""}
                    <div class="meta-row">${meta}</div>
                </div>
                <div class="todo-actions">
                    <button type="button" data-complete="${todo.id}" ${todo.status === "COMPLETED" ? "disabled" : ""}>完成</button>
                    <button type="button" data-edit="${todo.id}">编辑</button>
                    <button type="button" data-delete="${todo.id}">删除</button>
                </div>
            </article>`;
    }).join("") : '<div class="empty">清单还是空的，先添加一个今天要推进的小任务。</div>';
}

function renderNotes() {
    $("#noteList").innerHTML = state.notes.length ? state.notes.map((note) => `
        <article class="note-card" tabindex="0" title="${escapeHtml(note.content)}">
            <h3>${escapeHtml(note.title)}</h3>
            <p>${escapeHtml(note.content)}</p>
        </article>`).join("") : '<div class="empty">还没有便笺，记下第一个念头吧。</div>';
}

async function loadDashboard() {
    try {
        const [todos, notes] = await Promise.all([api("/api/todos"), api("/api/notes")]);
        state.todos = todos;
        state.notes = notes;
        renderTodos();
        renderNotes();
        $(".status").classList.remove("offline");
        $(".status").classList.add("online");
        $("#statusText").textContent = "服务运行正常";
    } catch (error) {
        $(".status").classList.remove("online");
        $(".status").classList.add("offline");
        $("#statusText").textContent = "服务连接失败";
        $("#todoList").innerHTML = '<div class="empty">暂时无法读取任务</div>';
        $("#noteList").innerHTML = '<div class="empty">暂时无法读取便笺</div>';
        console.warn("Dashboard data failed to load", error);
    }
}

function addMessage(role, content, loading = false) {
    const message = document.createElement("div");
    message.className = `message ${role}${loading ? " loading" : ""}`;
    const body = role === "assistant" && !loading
        ? `<div class="markdown-body">${renderMarkdown(content)}</div>`
        : `<p>${escapeHtml(content)}</p>`;
    message.innerHTML = `${role === "assistant" ? '<span class="avatar">LP</span>' : ""}<div>${body}</div>`;
    $("#chatStream").appendChild(message);
    $("#chatStream").scrollTop = $("#chatStream").scrollHeight;
    return message;
}

$("#chatForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const input = $("#chatInput");
    const message = input.value.trim();
    if (!message) return;
    addMessage("user", message);
    input.value = "";
    input.style.height = "auto";
    const button = event.currentTarget.querySelector("button");
    button.disabled = true;
    const loading = addMessage("assistant", "正在思考", true);
    try {
        const result = await api("/api/chat", { method: "POST", body: JSON.stringify({ conversationId: state.conversationId, message }) });
        state.conversationId = result.conversationId;
        loading.remove();
        addMessage("assistant", result.content);
        await loadDashboard();
    } catch (error) {
        loading.remove();
        addMessage("assistant", `没有处理成功：${error.message}`);
        showToast(error.message, true);
    } finally {
        button.disabled = false;
        input.focus();
    }
});

$("#chatInput").addEventListener("input", (event) => {
    event.target.style.height = "auto";
    event.target.style.height = `${Math.min(event.target.scrollHeight, 120)}px`;
});
$("#chatInput").addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        $("#chatForm").requestSubmit();
    }
});
$("#clearChat").addEventListener("click", () => {
    state.conversationId = null;
    $("#chatStream").innerHTML = "";
    addMessage("assistant", "新对话已经准备好了。今天想先处理什么？");
});

$("#todoForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const payload = readTodoForm("todo");
    if (!payload.title) return;
    try {
        const todo = await api("/api/todos", { method: "POST", body: JSON.stringify(payload) });
        state.todos.unshift(todo);
        renderTodos();
        event.currentTarget.reset();
        $("#todoPriority").value = "MEDIUM";
        showToast("任务已加入今日清单");
    } catch (error) {
        showToast(error.message, true);
    }
});

$("#todoList").addEventListener("click", async (event) => {
    const completeButton = event.target.closest("[data-complete]");
    const editButton = event.target.closest("[data-edit]");
    const deleteButton = event.target.closest("[data-delete]");

    if (completeButton) {
        completeButton.disabled = true;
        try {
            const updated = await api(`/api/todos/${completeButton.dataset.complete}/complete`, { method: "POST" });
            state.todos = state.todos.map((todo) => todo.id === updated.id ? updated : todo);
            renderTodos();
            showToast("任务已完成");
        } catch (error) {
            completeButton.disabled = false;
            showToast(error.message, true);
        }
    }

    if (editButton) {
        const todo = state.todos.find((item) => item.id === editButton.dataset.edit);
        if (!todo) return;
        $("#editTodoId").value = todo.id;
        $("#editTodoTitle").value = todo.title || "";
        $("#editTodoDescription").value = todo.description || "";
        $("#editTodoCategory").value = todo.category || "";
        $("#editTodoPriority").value = todo.priority || "MEDIUM";
        $("#editTodoEstimated").value = todo.estimatedMinutes ?? "";
        $("#editTodoPlannedStart").value = toInputDate(todo.plannedStartAt);
        $("#editTodoReminder").value = toInputDate(todo.reminderAt);
        $("#editTodoDialog").showModal();
    }

    if (deleteButton) {
        if (!confirm("确定删除这个任务吗？")) return;
        try {
            await api(`/api/todos/${deleteButton.dataset.delete}`, { method: "DELETE" });
            state.todos = state.todos.filter((todo) => todo.id !== deleteButton.dataset.delete);
            renderTodos();
            showToast("任务已删除");
        } catch (error) {
            showToast(error.message, true);
        }
    }
});

$("#editTodoForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const id = $("#editTodoId").value;
    const payload = readTodoForm("editTodo");
    try {
        const updated = await api(`/api/todos/${id}`, { method: "PUT", body: JSON.stringify(payload) });
        state.todos = state.todos.map((todo) => todo.id === updated.id ? updated : todo);
        renderTodos();
        $("#editTodoDialog").close();
        showToast("任务已更新");
    } catch (error) {
        showToast(error.message, true);
    }
});

$("#closeEditTodo").addEventListener("click", () => $("#editTodoDialog").close());
$("#editTodoDialog").addEventListener("click", (event) => {
    if (event.target === event.currentTarget) event.currentTarget.close();
});

$("#openNote").addEventListener("click", () => $("#noteDialog").showModal());
$("#closeNote").addEventListener("click", () => $("#noteDialog").close());
$("#noteDialog").addEventListener("click", (event) => {
    if (event.target === event.currentTarget) event.currentTarget.close();
});
$("#noteForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const title = $("#noteTitle").value.trim();
    const content = $("#noteContent").value.trim();
    try {
        const note = await api("/api/notes", { method: "POST", body: JSON.stringify({ title, content }) });
        state.notes.unshift(note);
        renderNotes();
        event.currentTarget.reset();
        $("#noteDialog").close();
        showToast("便笺已保存");
    } catch (error) {
        showToast(error.message, true);
    }
});

$("#todayLabel").textContent = new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long"
}).format(new Date());
loadDashboard();
