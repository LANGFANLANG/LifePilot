const assert = require("assert");
const fs = require("fs");
const path = require("path");
const vm = require("vm");

const rendererPath = path.join(__dirname, "..", "src", "main", "resources", "static", "markdown.js");
const sandbox = { window: {} };
vm.runInNewContext(fs.readFileSync(rendererPath, "utf8"), sandbox, { filename: rendererPath });

const render = sandbox.window.LifePilotMarkdown.renderMarkdown;
const html = render(`我是 LifePilot，你的个人执行规划助手！以下是我能帮你的事情：

**📋 待办管理**
-创建、查看、编辑、删除待办任务
-标记任务完成

## 🎯 目标规划
1. 拆解目标
2. 制定计划

---

| 功能 | 状态 |
| --- | --- |
| Markdown | 正常 |
`);

assert.match(html, /<h3>📋 待办管理<\/h3>/);
assert.match(html, /<ul><li>创建、查看、编辑、删除待办任务<\/li><li>标记任务完成<\/li><\/ul>/);
assert.match(html, /<h2>🎯 目标规划<\/h2>/);
assert.match(html, /<ol><li>拆解目标<\/li><li>制定计划<\/li><\/ol>/);
assert.match(html, /<hr>/);
assert.match(html, /<table>/);
assert.doesNotMatch(html, /\*\*📋 待办管理\*\*/);
assert.doesNotMatch(html, /-创建/);

console.log("Markdown renderer smoke test passed");
