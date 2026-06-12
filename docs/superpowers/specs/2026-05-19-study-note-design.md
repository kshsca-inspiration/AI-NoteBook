# 项目设计文档：AI 辅助学习笔记网站

## 一、项目概述

一个三栏布局的单页 Web 应用。用户在学习过程中与 AI 对话讨论，聊完 AI 自动生成结构化笔记。支持 Markdown 可视化编辑、笔记文件夹管理、搜索。每篇笔记绑定独立的 AI 聊天上下文，笔记内容和 AI 对话在同一界面展示。

## 二、技术栈

| 层级 | 技术 | 
|------|------|
| 前端 | Vue 3 (Composition API) + Vite + Axios + marked.js |
| 后端 | Spring Boot 3 + MyBatis-Plus + Maven |
| 数据库 | H2 (开发环境，内嵌免安装) |
| AI | DeepSeek API (OkHttp 调用) |
| 实时通信 | SSE (Server-Sent Events) 流式输出 AI 回复 |

## 三、核心功能

### 阶段一

1. **笔记管理**：新建/编辑/删除笔记，Markdown 格式存储
2. **文件夹管理**：创建文件夹，笔记归类，按文件夹筛选
3. **Markdown 可视化编辑器**：工具栏按钮（加粗/斜体/标题/划线/荧光笔/待办/序号/链接），源码/预览切换
4. **AI 聊天面板**：每篇笔记绑定独立聊天会话，SSE 流式输出，AI 始终携带当前笔记上下文
5. **聊天生成笔记**：聊完后 AI 根据聊天内容生成/更新结构化 Markdown 笔记
6. **搜索**：按关键词搜索笔记标题和内容
7. **AI 出题**：根据笔记内容生成自测题
8. **三栏可拖拽/可收起面板**：左(笔记列表) → 中(AI聊天) → 右(编辑器)

### 阶段二（后续迭代）

- 图片上传
- 独立待办列表组件
- 导出 PDF

## 四、页面布局

```
┌──────────┬──────────────────┬─────────────────────────┐
│ 笔记列表  │   AI 助手         │   笔记编辑器              │
│ (180px)  │   (260px)         │   (自适应)               │
│          │                  │                          │
│ 📁文件夹 │ 讨论「依赖注入」   │  [B] [I] [H1] [H2] [S]   │
│ 筛选     │                  │  [荧光] [☐] [1.] [🔗]    │
│          │  用户: 三种方式?   │                          │
│ ├ 笔记A  │  AI: 构造器注入..  │  ## 依赖注入原理          │
│ │(Spring)│                  │                          │
│ ├ 笔记B  │                  │  今天学习了 Spring Boot   │
│ │(Vue)   │                  │  的依赖注入原理...        │
│ └ 笔记C  │  [输入框][发送]   │                          │
│   (AI)   │                  │  [💾保存] [📄导出]        │
└──────────┴──────────────────┴─────────────────────────┘
```

三栏均可通过拖拽分隔条调整宽度，每栏可收起。

## 五、数据模型

```
Folder（文件夹）
├── id (Long, PK)
├── name (String)
└── created_at (LocalDateTime)

Note（笔记）
├── id (Long, PK)
├── title (String)
├── content (String, Markdown)
├── folder_id (Long, FK → Folder)
├── created_at (LocalDateTime)
└── updated_at (LocalDateTime)

ChatMessage（聊天消息）
├── id (Long, PK)
├── note_id (Long, FK → Note)
├── role (String, "user" 或 "ai")
├── content (String)
└── created_at (LocalDateTime)
```

ChatSession 不单独建表，通过 note_id 直接关联 ChatMessage。每篇笔记 = 一个聊天会话。

## 六、API 设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/folders | 文件夹列表 |
| POST | /api/folders | 新建文件夹 |
| GET | /api/notes | 笔记列表 (支持 ?folder_id= &keyword=) |
| POST | /api/notes | 新建笔记 |
| GET | /api/notes/:id | 笔记详情 |
| PUT | /api/notes/:id | 修改笔记 |
| DELETE | /api/notes/:id | 删除笔记 |
| GET | /api/notes/:id/messages | 获取聊天历史 |
| POST | /api/notes/:id/chat/send | 发送消息 (SSE 流式返回) |
| POST | /api/notes/:id/generate-note | 根据聊天生成笔记 |
| POST | /api/notes/:id/generate-quiz | AI 出题 |

## 七、前端组件树

```
App.vue
├── TopBar                          # logo、搜索、新建按钮
│   ├── SearchBox
│   └── NewNoteButton
├── ResizableLayout                 # 三栏可拖拽容器
│   ├── NoteListPanel               # 左栏：笔记列表
│   │   ├── FolderSelect            # 文件夹下拉筛选
│   │   └── NoteListItem[]          # 笔记条目
│   ├── ChatPanel                   # 中栏：AI 对话
│   │   ├── ChatHeader
│   │   ├── ChatMessageList
│   │   │   └── ChatMessage[]
│   │   └── ChatInput
│   └── EditorPanel                 # 右栏：笔记编辑器
│       ├── NoteTitle
│       ├── MarkdownToolbar
│       ├── MarkdownEditor (源码)
│       ├── MarkdownPreview (预览)
│       └── EditorFooter
```

## 八、后端项目结构

```
backend/src/main/java/com/studynote/
├── StudyNoteApplication.java       # 启动主类
├── config/
│   ├── CorsConfig.java             # 跨域配置
│   └── WebConfig.java              # Web 配置
├── controller/
│   ├── FolderController.java
│   ├── NoteController.java
│   └── ChatController.java
├── service/
│   ├── FolderService.java
│   ├── NoteService.java
│   ├── ChatService.java
│   └── DeepSeekService.java        # AI API 调用
├── mapper/
│   ├── FolderMapper.java
│   ├── NoteMapper.java
│   └── ChatMessageMapper.java
├── entity/
│   ├── Folder.java
│   ├── Note.java
│   └── ChatMessage.java
└── dto/
    ├── NoteRequest.java
    ├── ChatRequest.java
    └── AiResponse.java
```

## 九、前端项目结构

```
frontend/src/
├── App.vue
├── main.js
├── api/index.js                    # Axios 封装 + API 函数
├── stores/noteStore.js             # Pinia：当前笔记、笔记列表
├── components/
│   ├── TopBar.vue
│   ├── NoteListPanel.vue
│   ├── NoteListItem.vue
│   ├── ChatPanel.vue
│   ├── ChatMessage.vue
│   ├── ChatInput.vue
│   ├── EditorPanel.vue
│   ├── MarkdownToolbar.vue
│   ├── MarkdownPreview.vue
│   └── ResizableLayout.vue
└── assets/
    └── main.css                    # 全局样式 + 紫粉主题色
```

## 十、视觉风格

- **配色**：柔和紫粉渐变（#F8F6FF 背景 + #C8B8E8~#A890D0 主色 + #FFEAA7 荧光高亮）
- **字体**：衬线体（中文字体优先宋体/楷体，英文 serif）
- **图标**：简约圆形图标（纯色渐变填充）
- **风格**：韩系手账风，温馨柔和但不土气
