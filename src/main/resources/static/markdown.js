(function () {
    const escapeHtml = (value = "") => String(value).replace(/[&<>'"]/g, (char) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "'": "&#39;",
        "\"": "&quot;"
    })[char]);

    const unwrapFencedMarkdown = (text) => {
        const fenced = text.trim().match(/^```(?:md|markdown)?\s*\n([\s\S]*?)\n```$/i);
        return fenced ? fenced[1] : text;
    };

    const normalizeMarkdown = (value = "") => unwrapFencedMarkdown(String(value).replace(/\r\n/g, "\n"))
        .replace(/[ \t]+(#{1,6}\s*)/g, "\n\n$1")
        .replace(/([。；;:：])\s*((?:[-*+]\s*|\d+[.)]\s*))/g, "$1\n$2")
        .replace(/[ \t]+(>\s?)/g, "\n\n$1")
        .trim();

    const getLineType = (line) => {
        if (/^```/.test(line)) return "code";
        if (/^#{1,6}\s+/.test(line)) return "heading";
        if (/^\*\*[^*]+\*\*$/.test(line)) return "strong-heading";
        if (/^---+$/.test(line)) return "rule";
        if (/^\|.*\|$/.test(line)) return "table";
        if (/^([-*+]|\d+[.)])\s*/.test(line)) return "list";
        if (/^>\s?/.test(line)) return "quote";
        return "paragraph";
    };

    const pushBlock = (blocks, type, lines) => {
        if (type && lines.length) blocks.push({ type, lines });
    };

    const parseBlocks = (value = "") => {
        const lines = normalizeMarkdown(value).split("\n");
        const blocks = [];
        let currentType = null;
        let currentLines = [];
        let inCodeBlock = false;

        lines.forEach((rawLine) => {
            const line = inCodeBlock ? rawLine.replace(/\s+$/, "") : rawLine.trim();
            if (!inCodeBlock && !line) {
                pushBlock(blocks, currentType, currentLines);
                currentType = null;
                currentLines = [];
                return;
            }

            if (/^```/.test(line)) {
                if (!inCodeBlock) {
                    pushBlock(blocks, currentType, currentLines);
                    currentType = "code";
                    currentLines = [line];
                    inCodeBlock = true;
                    return;
                }
                currentLines.push(line);
                pushBlock(blocks, "code", currentLines);
                currentType = null;
                currentLines = [];
                inCodeBlock = false;
                return;
            }

            if (inCodeBlock) {
                currentLines.push(line);
                return;
            }

            const type = getLineType(line);
            if (currentType && type !== currentType) {
                pushBlock(blocks, currentType, currentLines);
                currentLines = [];
            }
            currentType = type;
            currentLines.push(line);
        });

        pushBlock(blocks, currentType, currentLines);
        return blocks;
    };

    const splitTableRow = (line) => line
        .replace(/^\|/, "")
        .replace(/\|$/, "")
        .split("|")
        .map((cell) => cell.trim());

    const isTableSeparator = (line) => {
        const cells = splitTableRow(line);
        return cells.length > 1 && cells.every((cell) => /^:?-{3,}:?$/.test(cell));
    };

    const parseTable = (lines) => {
        if (lines.length < 2 || !isTableSeparator(lines[1])) return null;
        return {
            headers: splitTableRow(lines[0]),
            rows: lines.slice(2)
                .filter((line) => /^\|.*\|$/.test(line))
                .map(splitTableRow)
        };
    };

    const renderInline = (value = "") => escapeHtml(value)
        .replace(/`([^`]+)`/g, "<code>$1</code>")
        .replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>")
        .replace(/__([^_]+)__/g, "<strong>$1</strong>")
        .replace(/~~([^~]+)~~/g, "<del>$1</del>")
        .replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+|mailto:[^)\s]+)\)/g, '<a href="$2" target="_blank" rel="noreferrer">$1</a>')
        .replace(/\*([^*]+)\*/g, "<em>$1</em>")
        .replace(/_([^_]+)_/g, "<em>$1</em>");

    const renderMarkdown = (value = "") => parseBlocks(value).map((block) => {
        const firstLine = block.lines[0] || "";
        if (block.type === "heading") {
            const rawLevel = firstLine.match(/^#+/)?.[0].length || 3;
            const level = Math.min(Math.max(rawLevel, 1), 6);
            return `<h${level}>${renderInline(firstLine.replace(/^#{1,6}\s+/, ""))}</h${level}>`;
        }
        if (block.type === "strong-heading") {
            return `<h3>${renderInline(firstLine.replace(/^\*\*|\*\*$/g, ""))}</h3>`;
        }
        if (block.type === "rule") {
            return "<hr>";
        }
        if (block.type === "list") {
            const ordered = block.lines.every((line) => /^\d+[.)]\s*/.test(line));
            const tag = ordered ? "ol" : "ul";
            const items = block.lines.map((line) => `<li>${renderInline(line.replace(/^([-*+]|\d+[.)])\s*/, ""))}</li>`).join("");
            return `<${tag}>${items}</${tag}>`;
        }
        if (block.type === "quote") {
            const quotes = block.lines.map((line) => `<p>${renderInline(line.replace(/^>\s?/, ""))}</p>`).join("");
            return `<blockquote>${quotes}</blockquote>`;
        }
        if (block.type === "code") {
            const language = firstLine.replace(/^```/, "").trim();
            const body = block.lines.slice(1, block.lines.at(-1)?.startsWith("```") ? -1 : undefined).join("\n");
            return `<pre>${language ? `<span class="code-language">${escapeHtml(language)}</span>` : ""}<code>${escapeHtml(body)}</code></pre>`;
        }
        if (block.type === "table") {
            const table = parseTable(block.lines);
            if (!table) return `<p>${renderInline(block.lines.join(" "))}</p>`;
            const header = table.headers.map((cell) => `<th>${renderInline(cell)}</th>`).join("");
            const rows = table.rows.map((row) => `<tr>${table.headers.map((_header, index) => `<td>${renderInline(row[index] || "")}</td>`).join("")}</tr>`).join("");
            return `<div class="table-scroll"><table><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table></div>`;
        }
        return `<p>${renderInline(block.lines.join(" "))}</p>`;
    }).join("");

    window.LifePilotMarkdown = {
        renderMarkdown,
        parseBlocks
    };
})();
