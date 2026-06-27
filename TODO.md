# TODO

## 已完成

- 桌面列布局常量已统一到 `tokens.css`（`--app-col-width`、`--app-col-edge`），`AppShell.vue` / `AiChat.vue` 通过 CSS 变量与 JS `layoutPx()` 引用；`720px` 断点仍在各组件 `@media` 中保持同步。
