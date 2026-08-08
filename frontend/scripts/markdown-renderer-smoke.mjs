import assert from 'node:assert'
import { renderMarkdown } from '../src/utils/markdown.js'

const html = renderMarkdown(`我是 LifePilot，你的个人执行规划助手！以下是我能帮你的事情：

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
`)

assert.match(html, /<h3>📋 待办管理<\/h3>/)
assert.match(html, /<ul><li>创建、查看、编辑、删除待办任务<\/li><li>标记任务完成<\/li><\/ul>/)
assert.match(html, /<h2>🎯 目标规划<\/h2>/)
assert.match(html, /<ol><li>拆解目标<\/li><li>制定计划<\/li><\/ol>/)
assert.match(html, /<hr>/)
assert.match(html, /<table>/)
assert.doesNotMatch(html, /\*\*📋 待办管理\*\*/)
assert.doesNotMatch(html, /-创建/)

console.log('Vue Markdown renderer smoke test passed')
