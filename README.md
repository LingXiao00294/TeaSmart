# TeaSmart 茶小智 ☕

> 一个面向茶饮场景的点单平台：用户端下单 + 管理后台运营 + AI 智能推荐与客服。
>
> 后端基于 **Spring Boot 3 + MyBatis-Plus + PostgreSQL**，前端基于 **Vue 3 + Vite + Element Plus**，AI 能力接入**阿里云 DashScope（通义千问）**。

---

## ✨ 功能特性

### 用户端

- 🛒 浏览菜单（按分类）、查看商品详情与多规格（杯型 / 糖度 / 冰度）
- 🧺 购物车增删改、按规格区分
- 📝 下单（待支付）→ 独立支付页确认 → 取消订单 / 查看订单详情
- 🤖 AI 智能推荐（自动选 3 款）+ AI 客服聊天（流式输出）
- 👤 个人中心、订单历史

### 管理后台（`/admin`，需 ADMIN 角色）

- 📊 Dashboard：营收 / 订单 / 用户等运营统计
- 🍱 商品管理（含规格、图片上传）、分类管理、轮播图管理
- 📋 订单管理（状态流转：待支付 → 已支付 → 制作中 → 已完成 / 已取消）
- 👥 用户管理、🧠 AI 知识库管理（喂给 AI 的领域语料）

---

## 🛠 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.3.5、Spring Security、MyBatis-Plus 3.5.7、Flyway、JWT（jjwt 0.12.6）、OkHttp 4 |
| 数据库 | PostgreSQL 15 |
| 前端 | Vue 3、Vue Router、Pinia、Element Plus、Axios、Vite 8 |
| AI | 阿里云 DashScope（OpenAI 兼容模式，默认模型 `qwen-turbo`） |
| 部署 | Docker、Docker Compose、Nginx |

---

## 📂 仓库地址

| 平台 | 地址 |
| --- | --- |
| GitHub | <https://github.com/LingXiao00294/TeaSmart> |

---

## 📦 快速开始

### 方式一：Docker Compose 一键启动（推荐）

```bash
cp .env.example .env          # 按需修改密码、JWT、AI Key 等
docker compose up --build
```

- 前端（Nginx）：<http://localhost/teasmart/>
- 后端 API：经 Nginx 反代到 `http://localhost/teasmart/api/`
- 数据库、上传目录均以 volume 持久化

### 方式二：本地开发

#### 1. 启动 PostgreSQL

```bash
cp .env.example .env          # docker compose 读取 DB_PASSWORD 等变量
docker compose up postgres          # 仅起数据库
# 或使用本地 PG，库名/用户均为 teasmart
```

#### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# 默认监听 :8080，Flyway 会自动执行建表与初始化数据
```

#### 3. 启动前端

```bash
cd frontend
bun install
bun run dev
# 开发服务器会自动把 /teasmart/api、/teasmart/uploads 代理到 localhost:8080
```

浏览器访问 Vite 输出的开发地址下的 `/teasmart/`。

---

## 🔑 默认账号

启动后 Flyway 会写入种子数据，内置管理员：

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `123456` | 管理员（ADMIN） |

普通用户可通过注册页创建（默认 `USER` 角色）。

---

## ⚙️ 环境变量

见 `.env.example`：

| 变量 | 说明 | 默认 / 示例 |
| --- | --- | --- |
| `DB_PASSWORD` | PostgreSQL 密码 | `teasmart` |
| `JWT_SECRET` | JWT 签名密钥（生产请使用强随机串） | — |
| `AI_API_KEY` | DashScope API Key（留空则 AI 功能降级） | — |
| `AI_BASE_URL` | AI 接口地址 | `https://dashscope.aliyuncs.com/compatible-mode/v1` |
| `AI_MODEL` | 模型名 | `qwen-turbo` |

> 未配置 `AI_API_KEY` 时：推荐接口返回内置 mock 数据，聊天接口会提示「AI 未配置」，系统其余功能不受影响。
>
> 后端还支持 `APP_BASE_PATH` 配置对外 URL 前缀，默认 `/teasmart`；当前 Docker Compose 部署在 `docker-compose.yml` 中固定传入 `/teasmart`，不由 `.env.example` 控制。

---

## 📁 项目结构

```text
TeaSmart/
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/com/teasmart/
│   │   ├── controller/           # REST 控制器（按业务域划分）
│   │   ├── service/              # 业务逻辑
│   │   ├── mapper/               # MyBatis-Plus Mapper
│   │   ├── entity/  dto/  vo/    # 实体 / 入参 / 出参
│   │   ├── config/               # Security、JWT、MyBatis、AI、WebMvc 配置
│   │   └── common/               # Result 统一响应、异常处理、JwtUtil
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/         # Flyway 迁移脚本 (V1__ 建表 / V2__ 初始数据 / V3__ 知识库)
├── frontend/                     # Vue 3 + Vite 前端
│   └── src/
│       ├── views/user/           # 用户端页面
│       ├── views/admin/          # 管理后台页面
│       ├── components/           # 通用组件（AiChat、AppHeader 等）
│       ├── api/  stores/  utils/ # 请求封装 / Pinia / 工具
│       └── assets/               # 设计 token + 全局样式（新中式茶韵）
├── docker/
│   ├── Dockerfile.frontend       # 前端多阶段构建 → Nginx
│   └── nginx.conf                # 反代 + SPA + SSE 配置
├── docker-compose.yml            # postgres + backend + frontend 编排
└── .env.example
```

---

## 🗄 数据库设计

核心表：`app_user`（用户）、`category`（分类）、`product`（商品）、`product_spec`（规格）、`cart_item` / `cart_item_spec`（购物车）、`tea_order` / `order_item`（订单）、`banner`（轮播）、`knowledge`（AI 知识库）。

> 注：用户表名为 `app_user`（`user` 是 PostgreSQL 保留字）；订单表名为 `tea_order`。

**订单状态机：**

```text
0 待支付 ──支付页确认──▶ 1 已支付 ──接单──▶ 2 制作中 ──完成──▶ 3 已完成
   │                        │
   └──取消──▶ 4 已取消 ◀──取消──┘
```

**下单与支付流程（前端）：**

下单与支付是两个独立步骤，订单创建后停留在「待支付」状态，直到用户主动支付或取消：

```text
购物车 ──下单──▶ 支付页 /pay/:id ──确认支付──▶ 订单详情（已支付）
                    └──取消订单──▶ 订单列表（已取消）
```

- 下单后进入独立支付页（`Payment.vue`，路由 `/pay/:id`），展示订单明细与合计
- 支付页提供「确认支付」（模拟 1.5s 处理后状态置为已支付）和「取消订单」两个操作
- 待支付订单在**订单列表卡片**和**订单详情页**均可通过「去支付」跳转到支付页
- 支付/取消均有错误处理与防重复提交保护

> 后端 `POST /api/orders/{id}/pay` 与 `PUT /api/orders/{id}/cancel` 已实现完整状态机校验，非法状态转换会返回业务错误。

表结构变更请**新增** Flyway 脚本（`V<序号>__名称.sql`），不要修改历史迁移。

---

## 📡 API 概览

> 除 `/api/ai/chat` 的 SSE 流式接口外，JSON 响应统一为 `Result<T>{ code, message, data }`，`code === 200` 表示成功。除公开接口外均需在请求头携带 `Authorization: Bearer <token>`。

| 模块 | 前缀 | 主要接口 |
| --- | --- | --- |
| 认证 | `/api/auth` | 注册、登录、获取当前用户 |
| 商品 | `/api/products` | 列表、详情 |
| 分类 | `/api/categories` | 列表 |
| 轮播 | `/api/banners` | 列表 |
| 购物车 | `/api/cart` | 查询、加入、修改数量、删除 |
| 订单 | `/api/orders` | 列表、详情、下单、支付、取消 |
| AI | `/api/ai` | 智能推荐、SSE 流式聊天 |
| 管理后台 | `/api/admin/**` | 商品 / 分类 / 轮播 / 知识库 / 订单状态 / 用户 / Dashboard 统计 / 文件上传（需 ADMIN） |
| 健康检查 | `/api/health` | 公开探活 |

---

## 🤖 AI 能力

- **智能推荐**：基于在售商品菜单，让大模型选出 3 款并给出推荐理由，严格校验返回的 `productId` 合法性。
- **AI 客服**：`/api/ai/chat` 采用 **SSE 流式**输出，逐 token 推送；该接口直接返回事件流，不使用 `Result<T>` 包装。
- **知识库增强**：管理后台维护的领域语料会作为 system prompt 注入，让 AI 回答更贴合本店商品。

> AI 走 **OkHttp** 直连 DashScope；聊天接口在 Nginx 下已配置 `proxy_buffering off` 以支持流式。

---

## 🌐 子路径部署说明

应用整体部署在 **`/teasmart/`** 子路径下，但**后端不感知该前缀**（context-path 为空），前缀统一由反向代理剥离。涉及四处需保持一致：

- 前端：`vite.config.js` 的 `base`、路由 `history`、Axios `baseURL`
- 反向代理：生产 `docker/nginx.conf` / 开发 `vite.config.js` 的 `server.proxy`
- 后端：仅在拼接对外 URL（如上传文件）时使用 `APP_BASE_PATH`

如需改到根路径或其他前缀，需同步修改以上位置。

---

## 🧪 开发约定

- 后端测试：`mvn test`，单个测试类 `mvn test -Dtest=BannerServiceTest`
- 前端构建：`bun run build`（产物输出到 `frontend/dist`）
- 提交信息遵循 Conventional Commits（中文描述），如 `feat(backend): …`、`fix(frontend): …`
