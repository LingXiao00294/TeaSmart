# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

TeaSmart（茶小智）——茶饮点单平台。Java 17 / Spring Boot 3.3.5 后端 + Vue 3 / Vite 前端，PostgreSQL 存储，集成阿里云 DashScope（Qwen）做 AI 推荐与聊天。分用户端（点单/购物车/订单/AI 助手）与管理后台（商品/分类/订单/用户/轮播/知识库/Dashboard）两部分。

## 常用命令

### 后端（`backend/`）

```bash
cd backend
mvn spring-boot:run                 # 本地运行（需 PostgreSQL，见下方）
mvn test                            # 全量测试
mvn test -Dtest=BannerServiceTest   # 单个测试类
mvn clean package -DskipTests       # 打 jar（target/*.jar，也是 Docker 构建产物）
```

### 前端（`frontend/`）

```bash
cd frontend
npm install
npm run dev        # Vite 开发服务器，自动代理 /teasmart/api、/teasmart/uploads → localhost:8080
npm run build      # 产物到 frontend/dist（base 固定为 /teasmart/）
npm run preview
```

### 全栈（Docker）

```bash
docker compose up --build          # 起 postgres + backend + frontend(nginx:80)
```

### 环境变量（见 `.env.example`）

`DB_PASSWORD`、`JWT_SECRET`、`AI_API_KEY`、`AI_BASE_URL`、`AI_MODEL`。本地跑后端需要可用的 PostgreSQL（`jdbc:postgresql://localhost:5432/teasmart`，库名/用户均为 `teasmart`），可用 `docker compose up postgres` 单独起一个。AI 功能未配 `AI_API_KEY` 时自动降级（见下）。

## 架构要点

### 子路径 `/teasmart` 部署（贯穿全栈，改动任一层都要同步）

应用整体部署在 `/teasmart/` 子路径下，但**后端 context-path 为空、完全不感知该前缀**——前缀由反向代理在转发时剥离。三处必须保持一致，缺一不可：

- **前端**：`vite.config.js` 的 `base: '/teasmart/'`、`router/index.js` 用 `createWebHistory(import.meta.env.BASE_URL)`、`utils/request.js` 的 `baseURL` 也基于 `BASE_URL`。
- **代理**：生产用 `docker/nginx.conf`，开发用 `vite.config.js` 的 `server.proxy`，二者都把 `/teasmart/api`、`/teasmart/uploads` 改写后转发到 `backend:8080`（或 `localhost:8080`），把 `/teasmart/` 映射到 SPA 静态文件。
- **后端唯一用到前缀的地方**：`app.base-path`（`APP_BASE_PATH`，默认 `/teasmart`），仅用于拼接对外暴露的 URL（如上传文件访问地址），**不要**用于路由/context-path。

### 后端分层（`com.teasmart`）

标准 Spring Boot 分层：`controller` → `service` → `mapper`（MyBatis-Plus）→ `entity`（`@TableName` 映射表）。请求/返回用 `dto`（入参）与 `vo`（出参）隔离实体。`common` 放 `Result`/`JwtUtil`/异常；`config` 放 Security、MyBatis、AI、WebMvc 配置。包名即角色，新增功能按此放置。

### 认证与授权

- **无状态 JWT**：`JwtAuthFilter` 从 `Authorization: Bearer <token>` 解析 `userId`/`username`/`role`，写入 `SecurityContext`（principal 为 userId，details 为 username，authority 为 `ROLE_<role>`）。
- **`SecurityConfig`**：`/api/auth/register`、`/api/auth/login`、`/api/health`、`/uploads/**` 公开；`/api/admin/**` 需 `ROLE_ADMIN`；其余需登录。无权访问由 `CustomAuthenticationEntryPoint` / `CustomAccessDeniedHandler` 处理。
- **角色**：`User.role` 为 `USER` 或 `ADMIN`。注意用户表名是 **`app_user`**（`User.java` 的 `@TableName("app_user")`），因 `user` 是 PostgreSQL 保留字。
- **前端守卫**：`router/index.js` 的 `beforeEach` 检查 token 与 `userStore.isAdmin()`；axios 响应拦截器见 `code===401` 自动登出并跳 `/login`。

### 统一响应格式

所有接口返回 `Result<T>{ code, message, data }`，**`code===200` 才算成功**（HTTP 状态码另算）。前端 `utils/request.js` 据此拆包：非 200 视为失败并 reject，401 触发登出。新增接口务必用 `Result.ok()/fail(...)`。

### 数据库（Flyway + MyBatis-Plus）

- **迁移**：`src/main/resources/db/migration/V*__*.sql`，`baseline-on-migrate=true`、`baseline-version=0`。改表结构就新增一个 `V<下一个编号>__xxx.sql`，不要手改历史迁移。
- **MyBatis-Plus**：`map-underscore-to-camel-case=true`、`id-type=auto`、`insert-strategy=not_null`。SQL 日志默认打到 stdout（`StdOutImpl`）。

### AI 集成（`AiService` + `AiConfig`）

- 调用阿里云 DashScope 的 **OpenAI 兼容模式**（`/chat/completions`），模型默认 `qwen-turbo`，经 **OkHttp**（不用 WebClient/RestTemplate）。
- `AiConfig.isAvailable()` 判断是否配置了 `AI_API_KEY`：**未配置时自动降级**——`recommend()` 返回 mock 数据，`chat()` 直接提示未配置。
- **聊天走 SSE 流式**：`AiController` 的 `/api/ai/chat` 返回 `SseEmitter`，逐 token 推送，结束发 `[DONE]`。nginx 对该 location 必须 `proxy_buffering off`（见 `nginx.conf`，已配置）。
- **知识库**：`KnowledgeService.buildKnowledgeContext()` 拼接进 system prompt，内容由后台「知识库管理」维护。AI 返回的 JSON 用宽松解析（容忍 ```代码块包裹、校验 productId 合法性）。

### 文件上传

上传落盘到 `upload.dir`（默认 `uploads`，Docker 中 `/app/uploads`，挂载为 volume），通过 `WebMvcConfig` 映射为 `/uploads/**` 静态资源。对外 URL 拼接 `app.base-path` 前缀。

### 前端设计系统

全站采用「新中式茶韵」设计系统：设计 token 集中在 `src/assets/tokens.css`，全局样式在 `src/assets/global.css`，字体用 `@fontsource`（Noto Serif/Sans SC、Cormorant Garamond）。改视觉风格优先动 `tokens.css`。组件库用 Element Plus。

## 约定

- 提交信息沿用 Conventional Commits 中文风格（如 `feat(frontend): …`、`fix(backend): …`、`build(docker): …`），见 git log。
- 前端别名 `@` → `frontend/src`。
- 后端默认开启 MyBatis-Plus SQL 日志到 stdout；本地排查 SQL 时直接看控制台。

### Git 工作流

- **禁止直接向 `main` 推送或合并**——所有改动必须通过 GitHub PR 合入。
- 开发在独立分支进行（命名如 `feat/xxx`、`fix/xxx`），完成后推送分支并创建 PR。
- PR 合并方式**只允许 Squash and merge**，禁止普通 merge 和 rebase/merge。
- 禁止在 main 上使用 `git reset`、`git push --force` 等改写历史的操作。
