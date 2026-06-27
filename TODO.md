# TODO

## 未处理的代码评审发现

### 桌面列布局常量三处重复（低优先级）

**问题**：`AiChat.vue` 的 JS 常量（`DESKTOP_COL=600`、`DESKTOP_BREAK=720`、`EDGE_MARGIN=18`）、AiChat 的 `@media (min-width: 720px)` CSS `calc(50% - 300px + 18px)`、以及 `AppShell.vue`（`width: 600px`、`@media min-width: 720px`）三处声明相同的布局参数，无共享契约。更改列宽或断点时需同步修改三处，遗漏任一将导致 FAB/聊天窗与居中列错位。

**建议**：在 `tokens.css` 中引入布局自定义属性（如 `--app-col-width`、`--app-col-breakpoint`），在 `AppShell.vue` 和 `AiChat.vue` 的 CSS 中通过 `var()` 引用；JS 常量（`edgeX()`）改为读取 CSS 变量或维护单一来源。`@media` 查询因 CSS 限制无法使用 `var()`，断点值需保持 JS/CSS 两侧一致（至少在注释中交叉引用）。
