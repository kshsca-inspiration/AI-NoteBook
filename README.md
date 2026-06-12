# 📒 StudyNote — AI 辅助学习笔记

一个**三栏布局的智能学习笔记平台**，集 Markdown 编辑器、AI 对话助手、自动笔记生成于一体。边学边聊，聊完 AI 帮你整理结构化笔记，还能自动出题自测——让知识真正内化。

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs" alt="Vue">
  <img src="https://img.shields.io/badge/Vite-8.0-646CFF?logo=vite" alt="Vite">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/H2-Embedded-0052CC" alt="H2">
  <img src="https://img.shields.io/badge/AI-DeepSeek-4B6BFB" alt="DeepSeek">
</p>

---

## ✨ 亮点速览

| 亮点 | 说明 |
|------|------|
| 🤖 **AI 深度集成** | 每篇笔记绑定独立 AI 会话，聊天上下文自动携带当前笔记内容；支持从对话中提炼结构化笔记、根据笔记内容自动出题 |
| ⚡ **SSE 流式响应** | AI 回复逐字实时输出，带脉动动画，体验丝滑；比 WebSocket 更轻量，前端直接消费 ReadableStream |
| 📝 **Markdown 所见即所得** | 源码/预览/分屏三种模式，分屏支持拖拽调节；富文本工具栏支持 20+ 格式，自动编号列表智能递增 |
| 📂 **文件夹归类** | 自由创建文件夹、拖拽笔记归类，下拉筛选 + 实时搜索，笔记再多也不乱 |
| 🎀 **韩系手账风设计** | 紫粉渐变配色（`#C8B8E8 → #A890D0`）、衬线字体、荧光高亮、圆角卡片——学习也要有温度 |
| 🔌 **开箱即用** | H2 文件数据库零配置启动，一台电脑即可跑通全栈；无需安装 MySQL/Redis |
| 🧩 **三栏可拖拽布局** | 笔记列表 ↔ AI 对话 ↔ 编辑器，分隔条自由拖拽缩放（120~600px），每栏可独立收起 |
| 🔥 **高亮扩展** | 自研 `==文本==` 荧光笔语法，渲染为 `<mark>` 标签，学习重点一目了然 |

---

## 🖥️ 页面布局

```
┌──────────────┬──────────────────┬───────────────────────────────┐
│  🔍 搜索笔记 │                  │                               │
│  ＋ 新建笔记  │   🤖 AI 助手    │   [B] [I] [H1] [H2] [S] [荧光] │
│              │                  │  [☐] [1.] [🔗] [📷] [📋]    │
│  📁 全部 ▾   │讨论「Spring依赖注入│                              │
│  ＋ 新文件夹  │                  │  ## 依赖注入的三种方式         │
│              │  👤 构造器注入和  │                               │
│  ├ 笔记 A    │     Setter 注入   │  构造器注入是 Spring           │
│  │ Spring    │    有什么区别？   │  官方推荐的方式，因为...        │
│  │ 2分钟前   │                   │                               │
│  ├ 笔记 B    │  🤖 构造器注入强制│      ┌─────────────────────┐  │
│  │ Vue       │     依赖不可变，  │      │  📑 目录            │  │
│  │ 1小时前   │     更符合... ███ │      │  依赖注入的三种方式   │  │
│  └ 笔记 C    │                  │       │ 构造器注入 vs Setter │  │
│    算法      │[输入消息...] [📨]│       └─────────────────────┘ │
│    昨天      │                  │                               │
│              │[📄生成笔记][↩撤销]│    [📝AI出题]      [💾保存] │
└──────────────┴──────────────────┴───────────────────────────────┘
    ↕ 可拖拽        ↕ 可拖拽             ↕ 自适应
   120~350px      180~600px            剩余宽度
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 最低版本 |
|------|----------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 18+ |
| npm | 9+ |

> ✅ 项目自带 H2 内嵌数据库，无需额外安装任何数据库。

### 1. 克隆项目

```bash
git clone <repo-url>
cd study
```

### 2. 启动后端（Spring Boot）

```bash
cd backend
mvn spring-boot:run
```

后端启动成功后：

```
Tomcat started on port 8080 (http)
Started StudyNoteApplication in 2.18 seconds
H2 console available at '/h2-console'
```

> 💡 H2 数据库控制台：浏览器打开 `http://localhost:8080/h2-console`，JDBC URL 填 `jdbc:h2:file:./data/studynote`，用户名 `sa`，密码留空。

### 3. 启动前端（Vite）

新开一个终端：

```bash
cd frontend
npm install
npm run dev
```

前端启动成功：

```
VITE v8.0.13  ready in 2414 ms
➜  Local:   http://localhost:5173/
```

### 4. 开始使用

浏览器访问 **`http://localhost:5173`**：

1. 新建一个笔记，输入标题
2. 在中间 AI 面板输入问题，与 AI 讨论
3. 点击 **「生成笔记」**，AI 将对话提炼为结构化 Markdown 追加到编辑器
4. 点击 **「AI 出题」**，自动生成 3-5 道自测题

---

## 🧱 技术架构

```
┌──────────────────────────────────────────┐     ┌──────────────────────────────────┐
│              Frontend (Vue 3)            │     │         Backend (Spring Boot 3)  │
│                                          │     │                                  │
│  ┌──────────┐  ┌────────────────────┐    │     │  ┌────────────┐  ┌─────────────┐ │
│  │  TopBar  │  │   Pinia Store      │    │HTTP │  │ Controller │  │   Service   │ │
│  │ 搜索/新建 │  │  folders/notes/    │    │───▶│  │ /api/*     │──│   CRUD +    │ │
│  └──────────┘  │  currentNote       │    │JSON │  └────────────┘  │   AI Logic  │ │
│                └────────────────────┘    │     │                  └──────┬──────┘ │
│  ┌──────────────────────────────────┐    │     │                         │        │
│  │     ResizableLayout (三栏)       │    │     │  ┌────────────┐  ┌──────▼──────┐ │
│  │  ┌────────┐┌───────┐┌────────┐   │    │SSE  │  │ MyBatis-   │    DeepSeek    │ │
│  │  │NoteList││ Chat  ││ Editor │   │    │◀───│  │ Plus Mapper│  │ Service     │─┼─▶ DeepSeek
│  │  │Panel   ││ Panel ││ Panel  │   │    │     │  └──────┬─────┘  │ (OkHttp)    │ │   API
│  │  └────────┘└───────┘└────────┘   │    │     │         │        └─────────────┘ │
│  └──────────────────────────────────┘    │     │  ┌──────▼─────┐                  │
│                                          │     │  │  H2 DB     │                  │
│  axios (REST)  +  fetch (SSE Stream)     │     │  │  (file)    │                  │
└──────────────────────────────────────────┘     │  └────────────┘                  │
        localhost:5173                           └──────────────────────────────────┘
        (Vite proxy /api → :8080)                        localhost:8080
```

### 技术选型

| 层级 | 技术 | 版本 | 选型理由 |
|------|------|------|----------|
| **前端框架** | Vue 3 + Composition API | 3.5 | `<script setup>` 语法简洁，响应式系统优秀 |
| **构建工具** | Vite | 8.0 | 秒级 HMR，原生 ESM 开发体验 |
| **状态管理** | Pinia | 3.0 | Vue 3 官方推荐，TypeScript 友好 |
| **Markdown** | marked | 18.0 | GFM 支持完善，可扩展自定义语法 |
| **后端框架** | Spring Boot | 3.2.5 | 生态成熟，SSE 支持开箱即用 |
| **ORM** | MyBatis-Plus | 3.5.7 | Lambda 查询 + 自动填充，效率远超 JPA |
| **数据库** | H2 (file) | — | 零配置，数据持久化到文件，开发体验极佳 |
| **AI 调用** | OkHttp | 4.12 | 轻量 HTTP 客户端，支持流式读取 |
| **实时通信** | SSE (SseEmitter) | — | 单向流式传输首选，比 WebSocket 更轻量 |

---

## 📡 API 一览

> 所有接口前缀 `/api`，前端通过 Vite proxy 转发至 `localhost:8080`

### 文件夹

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/folders` | 获取所有文件夹（按创建时间倒序） |
| `POST` | `/api/folders` | 新建文件夹 `{"name":"..."}` |

### 笔记

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/notes` | 笔记列表，支持 `?folderId=N&keyword=关键词` |
| `POST` | `/api/notes` | 新建笔记 `{"title":"...","content":"...","folderId":N}` |
| `GET` | `/api/notes/{id}` | 获取笔记详情 |
| `PUT` | `/api/notes/{id}` | 更新笔记（支持部分更新） |
| `DELETE` | `/api/notes/{id}` | 删除笔记（级联删除聊天记录） |

### AI 对话

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/notes/{id}/messages` | 获取该笔记的聊天历史 |
| `POST` | `/api/notes/{id}/chat/send` | 发送消息，**SSE 流式返回** AI 回复 |
| `POST` | `/api/notes/{id}/generate-note` | AI 根据聊天记录生成结构化笔记 |
| `POST` | `/api/notes/{id}/generate-quiz` | AI 根据笔记内容出题（选择+简答） |

---

## 📁 项目结构

```
study/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # Maven 依赖配置
│   └── src/main/
│       ├── java/com/studynote/
│       │   ├── StudyNoteApplication.java    # 启动类
│       │   ├── config/CorsConfig.java       # CORS 跨域配置
│       │   ├── controller/                  # REST 控制器
│       │   │   ├── FolderController.java    #   文件夹接口
│       │   │   ├── NoteController.java      #   笔记 CRUD 接口
│       │   │   └── ChatController.java      #   AI 聊天 + SSE 接口
│       │   ├── service/                     # 业务逻辑层
│       │   │   ├── FolderService.java
│       │   │   ├── NoteService.java
│       │   │   ├── ChatService.java         #   聊天业务 + 生成笔记/出题
│       │   │   └── DeepSeekService.java     #   DeepSeek API 调用（OkHttp）
│       │   ├── mapper/                      # MyBatis-Plus Mapper
│       │   ├── entity/                      # 数据实体
│       │   │   ├── Folder.java
│       │   │   ├── Note.java
│       │   │   └── ChatMessage.java
│       │   └── dto/                         # 请求/响应 DTO
│       └── resources/
│           ├── application.yml              # 应用配置
│           └── schema.sql                   # 建表 DDL
│
├── frontend/                          # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js                 # Vite 配置 + API 代理
│   └── src/
│       ├── main.js                    # 入口：createApp + Pinia
│       ├── App.vue                    # 根组件：三栏布局
│       ├── api/index.js               # Axios 封装 + SSE fetch 流式读取
│       ├── stores/noteStore.js        # Pinia 状态管理
│       ├── utils/markedConfig.js      # marked 配置 + ==高亮==扩展
│       ├── assets/main.css            # 全局样式 + 紫粉主题变量
│       └── components/
│           ├── TopBar.vue             #   顶栏：Logo、搜索、按钮
│           ├── ResizableLayout.vue    #   三栏可拖拽容器
│           ├── NoteListPanel.vue      #   左栏：笔记列表 + 文件夹
│           ├── NoteListItem.vue       #   笔记条目卡片
│           ├── ChatPanel.vue          #   中栏：AI 对话面板
│           ├── ChatMessage.vue        #   聊天气泡（Markdown 渲染）
│           ├── ChatInput.vue          #   消息输入框
│           ├── EditorPanel.vue        #   右栏：编辑器面板
│           └── MarkdownToolbar.vue    #   Markdown 工具栏
│
└── docs/                              # 项目文档
    └── superpowers/
        ├── specs/                     # 设计规格说明
        └── plans/                     # 实现计划
```

---

## 🗄️ 数据模型

```
Folder ─────────────┐
  id (PK)           │ 1:N, ON DELETE SET NULL
  name              │
  created_at        │
                     ▼
                    Note ──────────────┐
                      id (PK)          │ 1:N, ON DELETE CASCADE
                      title            │
                      content (Markdown)│
                      folder_id (FK)   │
                      created_at       │
                      updated_at       ▼
                                    ChatMessage
                                      id (PK)
                                      note_id (FK)
                                      role ("user" | "ai")
                                      content
                                      created_at
```

> 💡 **设计巧思**：ChatSession 不单独建表，通过 `note_id` 直接关联 ChatMessage。"一篇笔记 = 一个聊天会话"——模型简洁，查询高效。

---

## 🎨 设计亮点

### 🔥 自定义荧光笔语法

```markdown
这段是普通文字，==这段是重点高亮==，继续普通文字。
```

渲染为带黄色背景的 `<mark>` 标签，比加粗更醒目，适合学习笔记场景。

### 📝 智能编号列表

输入 `1. 第一点` 后按 Enter，自动续号 `2. `；支持三种编号格式：

| 格式 | 示例 |
|------|------|
| `1.` | `1. 2. 3.` |
| `1)` | `1) 2) 3)` |
| `(1)` | `(1) (2) (3)` |
| 罗马数字 | `I. II. III.` |
| 字母 | `a. b. c.` |

空行按 Enter 自动清除编号，流畅不打断思路。

### 🖼️ 图片插入尺寸预设

工具栏点击图片按钮，粘贴 URL 的同时可选择尺寸：

| 预设 | 宽度 |
|------|------|
| S | 200px |
| M | 400px |
| L | 600px |
| 原图 | 不设限制 |

### 📑 浮动目录面板

编辑器自动解析 H1~H3 标题生成目录，以滑入面板展示。点击条目自动跳转到对应位置（源码模式跳到对应行，预览模式滚动到标题）。

---

## 🔧 配置说明

### 后端配置（`backend/src/main/resources/application.yml`）

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/studynote;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true          # 开发环境开启 H2 控制台
  sql:
    init:
      mode: always           # 每次启动执行 schema.sql

deepseek:
  api:
    key: your-api-key        # DeepSeek API Key
    url: https://api.deepseek.com/chat/completions
```

### 前端配置（`frontend/vite.config.js`）

```js
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 🗺️ 路线图

- [x] 笔记 CRUD + 文件夹管理
- [x] Markdown 编辑器 + 工具栏 + 分屏预览
- [x] AI 聊天面板 + SSE 流式输出
- [x] AI 从聊天记录生成结构化笔记
- [x] AI 根据笔记内容自动出题
- [x] 笔记搜索（标题+内容）
- [x] 三栏可拖拽 + 可收起布局
- [x] 浮动目录（TOC）面板
- [x] `==高亮==` 自定义 Markdown 扩展
- [ ] 图片上传（粘贴/拖拽）
- [ ] 导出 PDF
- [ ] 独立待办列表组件
- [ ] 多 AI 模型切换
- [ ] 夜间模式

---

## 📄 License

MIT

---

<p align="center">
  <sub>Built with ❤️ by study-note team</sub>
</p>
