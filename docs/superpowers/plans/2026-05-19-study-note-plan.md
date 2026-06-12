# AI 辅助学习笔记网站 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个三栏布局的 AI 辅助学习笔记站点：左侧笔记列表，中间 AI 聊天面板，右侧 Markdown 笔记编辑器。每篇笔记绑定独立 AI 对话上下文。

**Architecture:** Spring Boot 3 后端通过 REST API + SSE 提供服务，Vue 3 前端通过 Axios/fetch 消费。DeepSeek API 通过 OkHttp 调用，SSE 流式返回 AI 回复。H2 内嵌数据库零配置启动。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, H2, OkHttp, Vue 3 (Composition API), Vite, Axios, marked.js, Pinia

---

## 前置准备

### Task 0: 项目根目录与 .gitignore

**Files:**
- Create: `d:/codeProject/study/.gitignore`

- [ ] **Step 1: 创建 .gitignore**

```
.superpowers/
node_modules/
target/
*.class
*.jar
!.mvn/wrapper/maven-wrapper.jar
.idea/
*.iml
.vscode/
*.log
```

- [ ] **Step 2: 验证**

```bash
ls d:/codeProject/study/.gitignore
```

---

## 阶段一：后端搭建

### Task 1: Spring Boot 项目脚手架

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/studynote/StudyNoteApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>
    <groupId>com.studynote</groupId>
    <artifactId>study-note-backend</artifactId>
    <version>1.0.0</version>
    <name>study-note-backend</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>3.5.7</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.12.0</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
package com.studynote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.studynote.mapper")
public class StudyNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyNoteApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/studynote;DB_CLOSE_DELAY=-1;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

deepseek:
  api-key: your-api-key-here
  api-url: https://api.deepseek.com/chat/completions
```

- [ ] **Step 4: 创建 schema.sql**

```sql
CREATE TABLE IF NOT EXISTS folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL DEFAULT '未命名笔记',
    content TEXT,
    folder_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    role VARCHAR(10) NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE
);
```

- [ ] **Step 5: 编译验证**

```bash
cd d:/codeProject/study/backend && mvn compile -q
```

预期：BUILD SUCCESS

- [ ] **Step 6: 启动验证**

```bash
cd d:/codeProject/study/backend && mvn spring-boot:run &
sleep 8
curl http://localhost:8080/h2-console -s -o /dev/null -w "%{http_code}"
```

预期输出：302 或 200（H2 控制台可访问）

---

### Task 2: 实体类 + DTO + Mapper

**Files:**
- Create: `backend/src/main/java/com/studynote/entity/Folder.java`
- Create: `backend/src/main/java/com/studynote/entity/Note.java`
- Create: `backend/src/main/java/com/studynote/entity/ChatMessage.java`
- Create: `backend/src/main/java/com/studynote/mapper/FolderMapper.java`
- Create: `backend/src/main/java/com/studynote/mapper/NoteMapper.java`
- Create: `backend/src/main/java/com/studynote/mapper/ChatMessageMapper.java`
- Create: `backend/src/main/java/com/studynote/dto/NoteRequest.java`
- Create: `backend/src/main/java/com/studynote/dto/ChatRequest.java`

- [ ] **Step 1: 创建 Folder 实体**

```java
package com.studynote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("folder")
public class Folder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建 Note 实体**

```java
package com.studynote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long folderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 ChatMessage 实体**

```java
package com.studynote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: 创建三个 Mapper 接口**

```java
// FolderMapper.java
package com.studynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studynote.entity.Folder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FolderMapper extends BaseMapper<Folder> {
}
```

```java
// NoteMapper.java
package com.studynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studynote.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
```

```java
// ChatMessageMapper.java
package com.studynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studynote.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
```

- [ ] **Step 5: 创建 DTO**

```java
// NoteRequest.java
package com.studynote.dto;

import lombok.Data;

@Data
public class NoteRequest {
    private String title;
    private String content;
    private Long folderId;
}
```

```java
// ChatRequest.java
package com.studynote.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String content;
}
```

- [ ] **Step 6: 编译验证**

```bash
cd d:/codeProject/study/backend && mvn compile -q
```

预期：BUILD SUCCESS

---

### Task 3: CORS 配置

**Files:**
- Create: `backend/src/main/java/com/studynote/config/CorsConfig.java`

- [ ] **Step 1: 创建跨域配置**

```java
package com.studynote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd d:/codeProject/study/backend && mvn compile -q
```

---

### Task 4: Folder + Note Service & Controller

**Files:**
- Create: `backend/src/main/java/com/studynote/service/FolderService.java`
- Create: `backend/src/main/java/com/studynote/service/NoteService.java`
- Create: `backend/src/main/java/com/studynote/controller/FolderController.java`
- Create: `backend/src/main/java/com/studynote/controller/NoteController.java`

- [ ] **Step 1: 创建 FolderService**

```java
package com.studynote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studynote.entity.Folder;
import com.studynote.mapper.FolderMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FolderService {
    private final FolderMapper folderMapper;

    public FolderService(FolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }

    public List<Folder> list() {
        return folderMapper.selectList(
            new LambdaQueryWrapper<Folder>().orderByDesc(Folder::getCreatedAt)
        );
    }

    public Folder create(String name) {
        Folder folder = new Folder();
        folder.setName(name);
        folderMapper.insert(folder);
        return folder;
    }
}
```

- [ ] **Step 2: 创建 NoteService**

```java
package com.studynote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studynote.entity.Note;
import com.studynote.mapper.NoteMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {
    private final NoteMapper noteMapper;

    public NoteService(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    public List<Note> list(Long folderId, String keyword) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (folderId != null) {
            wrapper.eq(Note::getFolderId, folderId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getContent, keyword));
        }
        wrapper.orderByDesc(Note::getUpdatedAt);
        return noteMapper.selectList(wrapper);
    }

    public Note getById(Long id) {
        return noteMapper.selectById(id);
    }

    public Note create(String title, String content, Long folderId) {
        Note note = new Note();
        note.setTitle(title != null ? title : "未命名笔记");
        note.setContent(content != null ? content : "");
        note.setFolderId(folderId);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return note;
    }

    public void update(Long id, String title, String content, Long folderId) {
        Note note = noteMapper.selectById(id);
        if (note == null) return;
        if (title != null) note.setTitle(title);
        if (content != null) note.setContent(content);
        if (folderId != null) note.setFolderId(folderId);
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
    }

    public void delete(Long id) {
        noteMapper.deleteById(id);
    }
}
```

- [ ] **Step 3: 创建 FolderController**

```java
package com.studynote.controller;

import com.studynote.entity.Folder;
import com.studynote.service.FolderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {
    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public List<Folder> list() {
        return folderService.list();
    }

    @PostMapping
    public Folder create(@RequestBody Map<String, String> body) {
        return folderService.create(body.get("name"));
    }
}
```

- [ ] **Step 4: 创建 NoteController**

```java
package com.studynote.controller;

import com.studynote.dto.NoteRequest;
import com.studynote.entity.Note;
import com.studynote.service.NoteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<Note> list(
        @RequestParam(required = false) Long folderId,
        @RequestParam(required = false) String keyword
    ) {
        return noteService.list(folderId, keyword);
    }

    @GetMapping("/{id}")
    public Note getById(@PathVariable Long id) {
        return noteService.getById(id);
    }

    @PostMapping
    public Note create(@RequestBody NoteRequest req) {
        return noteService.create(req.getTitle(), req.getContent(), req.getFolderId());
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody NoteRequest req) {
        noteService.update(id, req.getTitle(), req.getContent(), req.getFolderId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        noteService.delete(id);
    }
}
```

- [ ] **Step 5: 编译 + 启动后端，用 curl 测试 CRUD**

```bash
cd d:/codeProject/study/backend && mvn compile -q
```

预期：BUILD SUCCESS

```bash
# 启动后端（如已启动则跳过）
cd d:/codeProject/study/backend && mvn spring-boot:run &
sleep 8

# 测试 - 创建文件夹
curl -X POST http://localhost:8080/api/folders \
  -H "Content-Type: application/json" \
  -d '{"name":"Spring Boot学习"}'

# 测试 - 创建笔记
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title":"依赖注入","content":"## 依赖注入\n三种方式","folderId":1}'

# 测试 - 获取笔记列表
curl http://localhost:8080/api/notes

# 测试 - 搜索
curl "http://localhost:8080/api/notes?keyword=依赖"

# 测试 - 更新笔记
curl -X PUT http://localhost:8080/api/notes/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"依赖注入原理","content":"## 更新后的内容"}'

# 测试 - 删除笔记
curl -X DELETE http://localhost:8080/api/notes/1
```

预期：每个接口返回正确 JSON，删除后列表不再包含该笔记

---

### Task 5: DeepSeek AI + Chat Service & Controller

**Files:**
- Create: `backend/src/main/java/com/studynote/service/DeepSeekService.java`
- Create: `backend/src/main/java/com/studynote/service/ChatService.java`
- Create: `backend/src/main/java/com/studynote/controller/ChatController.java`
- Create: `backend/src/main/java/com/studynote/dto/QuizRequest.java`

- [ ] **Step 1: 创建 DeepSeekService（同步调用 + 流式调用）**

```java
package com.studynote.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class DeepSeekService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    /** 同步调用，返回完整回复 */
    public String chat(String systemPrompt, String userMessage) {
        String body = buildRequestBody(systemPrompt, userMessage, false);
        Request request = new Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(body, MediaType.parse("application/json")))
            .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = response.body().string();
            JsonNode root = objectMapper.readTree(respBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (IOException e) {
            return "AI 调用失败：" + e.getMessage();
        }
    }

    /** 流式调用，通过 consumer 逐块输出 */
    public void chatStream(String systemPrompt, String userMessage, Consumer<String> onChunk) {
        String body = buildRequestBody(systemPrompt, userMessage, true);
        Request request = new Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(body, MediaType.parse("application/json")))
            .build();

        try (Response response = client.newCall(request).execute()) {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body().byteStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                    String json = line.substring(6);
                    JsonNode node = objectMapper.readTree(json);
                    JsonNode delta = node.path("choices").get(0).path("delta").path("content");
                    if (!delta.isMissingNode()) {
                        onChunk.accept(delta.asText());
                    }
                }
            }
        } catch (IOException e) {
            onChunk.accept("[错误] " + e.getMessage());
        }
    }

    private String buildRequestBody(String systemPrompt, String userMessage, boolean stream) {
        try {
            Map<String, Object> systemMsg = Map.of("role", "system", "content", systemPrompt);
            Map<String, Object> userMsg = Map.of("role", "user", "content", userMessage);
            Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(systemMsg, userMsg),
                "stream", stream
            );
            return objectMapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: 创建 ChatService**

```java
package com.studynote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studynote.entity.ChatMessage;
import com.studynote.entity.Note;
import com.studynote.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.function.Consumer;

@Service
public class ChatService {
    private final ChatMessageMapper chatMessageMapper;
    private final NoteService noteService;
    private final DeepSeekService deepSeekService;

    public ChatService(ChatMessageMapper chatMessageMapper, NoteService noteService, DeepSeekService deepSeekService) {
        this.chatMessageMapper = chatMessageMapper;
        this.noteService = noteService;
        this.deepSeekService = deepSeekService;
    }

    /** 获取某个笔记的聊天历史 */
    public List<ChatMessage> getMessages(Long noteId) {
        return chatMessageMapper.selectList(
            new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getNoteId, noteId)
                .orderByAsc(ChatMessage::getCreatedAt)
        );
    }

    /** 流式聊天：保存用户消息 → 流式回调 → 保存 AI 回复 */
    public void chatStream(Long noteId, String userMessage, Consumer<String> onChunk) {
        // 1. 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setNoteId(noteId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        chatMessageMapper.insert(userMsg);

        // 2. 构建系统提示词（带上当前笔记内容作为上下文）
        Note note = noteService.getById(noteId);
        String systemPrompt = "你是一个学习助手。用户正在编辑一篇笔记，请基于笔记内容帮助用户学习。" +
            "笔记标题：" + (note != null ? note.getTitle() : "") + "\n" +
            "笔记内容：\n" + (note != null ? note.getContent() : "");

        // 3. 流式调用 AI，收集完整回复
        StringBuilder fullReply = new StringBuilder();
        deepSeekService.chatStream(systemPrompt, userMessage, chunk -> {
            fullReply.append(chunk);
            onChunk.accept(chunk);
        });

        // 4. 保存 AI 回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setNoteId(noteId);
        aiMsg.setRole("ai");
        aiMsg.setContent(fullReply.toString());
        chatMessageMapper.insert(aiMsg);
    }

    /** 根据聊天历史生成笔记 */
    public String generateNote(Long noteId) {
        List<ChatMessage> messages = getMessages(noteId);
        StringBuilder chatLog = new StringBuilder();
        for (ChatMessage msg : messages) {
            chatLog.append(msg.getRole().equals("user") ? "用户" : "AI").append("：")
                   .append(msg.getContent()).append("\n\n");
        }
        String prompt = "以下是用户和AI的学习讨论记录，请将其整理成一篇结构化的Markdown笔记，" +
            "包含标题、要点归纳、知识体系梳理。保留原文中的关键代码和术语：\n\n" + chatLog.toString();
        return deepSeekService.chat("你是学习笔记整理助手，只输出Markdown格式笔记不要额外解释。", prompt);
    }

    /** AI 出题 */
    public String generateQuiz(Long noteId) {
        Note note = noteService.getById(noteId);
        String prompt = "请基于以下笔记内容，生成3-5道自测题（包含选择题和简答题），" +
            "帮助用户检验学习效果。每题后附答案。\n\n笔记标题：" + note.getTitle() +
            "\n笔记内容：\n" + note.getContent();
        return deepSeekService.chat("你是出题助手，直接输出题目不要额外解释。", prompt);
    }
}
```

- [ ] **Step 3: 创建 ChatController**

```java
package com.studynote.controller;

import com.studynote.dto.ChatRequest;
import com.studynote.entity.ChatMessage;
import com.studynote.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes/{noteId}")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages")
    public List<ChatMessage> getMessages(@PathVariable Long noteId) {
        return chatService.getMessages(noteId);
    }

    @PostMapping(value = "/chat/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@PathVariable Long noteId, @RequestBody ChatRequest req) {
        SseEmitter emitter = new SseEmitter(300000L);
        chatService.chatStream(noteId, req.getContent(), chunk -> {
            try {
                emitter.send(SseEmitter.event().data(chunk));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        emitter.complete();
        return emitter;
    }

    @PostMapping("/generate-note")
    public Map<String, String> generateNote(@PathVariable Long noteId) {
        String content = chatService.generateNote(noteId);
        return Map.of("content", content);
    }

    @PostMapping("/generate-quiz")
    public Map<String, String> generateQuiz(@PathVariable Long noteId) {
        String quiz = chatService.generateQuiz(noteId);
        return Map.of("content", quiz);
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
cd d:/codeProject/study/backend && mvn compile -q
```

预期：BUILD SUCCESS

---

### Task 6: 后端启动 + API 集成测试

- [ ] **Step 1: 启动服务**

```bash
cd d:/codeProject/study/backend && mvn spring-boot:run &
sleep 10
```

- [ ] **Step 2: 端到端流程测试**

```bash
# 1. 创建文件夹
curl -s -X POST http://localhost:8080/api/folders \
  -H "Content-Type: application/json" -d '{"name":"Spring Boot"}'

# 2. 创建笔记
curl -s -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title":"依赖注入","content":"学习DI原理","folderId":1}'

# 3. 获取聊天历史（应该为空）
curl -s http://localhost:8080/api/notes/1/messages

# 4. 测试 AI 出题（需要配置 DeepSeek API Key）
curl -s -X POST http://localhost:8080/api/notes/1/generate-quiz

# 5. 测试生成笔记
curl -s -X POST http://localhost:8080/api/notes/1/generate-note
```

预期：步骤 1-3 返回正确 JSON，步骤 4-5 取决于 API Key 是否配置

---

## 阶段二：前端搭建

### Task 7: Vue 3 项目脚手架

**Files:**
- Create: 整个 `frontend/` 目录

- [ ] **Step 1: 创建 Vite + Vue 3 项目**

```bash
cd d:/codeProject/study
npm create vite@latest frontend -- --template vue
```

- [ ] **Step 2: 安装依赖**

```bash
cd d:/codeProject/study/frontend
npm install
npm install axios marked pinia
```

- [ ] **Step 3: 配置 Vite 代理（解决开发环境跨域）**

创建 `frontend/vite.config.js`（覆盖默认的 vite.config.js）：

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

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

- [ ] **Step 4: 启动前端验证**

```bash
cd d:/codeProject/study/frontend && npm run dev &
sleep 5
curl -s http://localhost:5173 | head -20
```

预期：返回 HTML 页面内容

- [ ] **Step 5: 清理模板文件**

```bash
# 删除 Vite 默认生成的示例文件
rm -f d:/codeProject/study/frontend/src/components/HelloWorld.vue
rm -f d:/codeProject/study/frontend/src/assets/vue.svg
```

预期：`src/components/` 和 `src/assets/` 被清空

---

### Task 8: 全局样式 + API 封装 + Pinia Store

**Files:**
- Create: `frontend/src/assets/main.css`
- Create: `frontend/src/api/index.js`
- Create: `frontend/src/stores/noteStore.js`

- [ ] **Step 1: 创建全局样式 main.css**

```css
/* 全局变量 - 紫粉主题色 */
:root {
  --bg-main: #F8F6FF;
  --bg-panel: #F5F0FA;
  --bg-card: #FFFFFF;
  --border: #E8E0F0;
  --primary: #A890D0;
  --primary-light: #C8B8E8;
  --primary-dark: #4A3F7A;
  --text: #4A4060;
  --text-light: #7B6F9B;
  --text-muted: #B8A8D0;
  --highlight: #FFEAA7;
  --pink-light: #FFE0F0;
  --pink: #E8B4B8;
  --font-serif: 'Georgia', 'Noto Serif SC', 'STSong', 'SimSun', serif;
  --font-sans: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --radius: 8px;
  --radius-lg: 12px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  font-family: var(--font-serif);
  background: var(--bg-main);
  color: var(--text);
  height: 100vh;
  overflow: hidden;
}

#app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 通用按钮 */
.btn {
  border: 1px solid var(--border);
  background: var(--bg-card);
  border-radius: 6px;
  padding: 6px 14px;
  font-size: 12px;
  cursor: pointer;
  font-family: var(--font-sans);
  color: var(--text);
  transition: all 0.15s;
}
.btn:hover { background: #F0ECF8; }
.btn-primary {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  color: white;
  border: none;
}
.btn-primary:hover { opacity: 0.9; }

/* 输入框 */
.input {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 8px 12px;
  font-size: 13px;
  font-family: var(--font-serif);
  color: var(--text);
  outline: none;
  width: 100%;
}
.input:focus { border-color: var(--primary-light); }

/* 滚动条美化 */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: var(--primary-light); border-radius: 3px; }

/* 标签 */
.tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-family: var(--font-sans);
}
.tag-purple { background: linear-gradient(135deg, #E8E0FF, #D5CCF0); color: #5B4D8B; }
.tag-pink { background: linear-gradient(135deg, #FFE0F0, #F5CCDD); color: #8B4D6B; }
```

- [ ] **Step 2: 创建 API 封装 api/index.js**

```js
import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// ===== 文件夹 =====
export const getFolders = () => http.get('/folders').then(r => r.data)
export const createFolder = (name) => http.post('/folders', { name }).then(r => r.data)

// ===== 笔记 =====
export const getNotes = (folderId, keyword) => {
  const params = {}
  if (folderId) params.folderId = folderId
  if (keyword) params.keyword = keyword
  return http.get('/notes', { params }).then(r => r.data)
}
export const getNote = (id) => http.get(`/notes/${id}`).then(r => r.data)
export const createNote = (data) => http.post('/notes', data).then(r => r.data)
export const updateNote = (id, data) => http.put(`/notes/${id}`, data)
export const deleteNote = (id) => http.delete(`/notes/${id}`)

// ===== 聊天 =====
export const getMessages = (noteId) => http.get(`/notes/${noteId}/messages`).then(r => r.data)

// SSE 流式聊天用原生 fetch（Axios 不支持流式）
export function chatStream(noteId, content, onChunk, onDone, onError) {
  fetch(`/api/notes/${noteId}/chat/send`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content })
  }).then(async (response) => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          onChunk(line.slice(5).trim())
        }
      }
    }
    if (buffer.trim()) {
      if (buffer.startsWith('data:')) onChunk(buffer.slice(5).trim())
    }
    onDone()
  }).catch(onError)
}

// ===== AI 功能 =====
export const generateNote = (noteId) =>
  http.post(`/notes/${noteId}/generate-note`).then(r => r.data.content)
export const generateQuiz = (noteId) =>
  http.post(`/notes/${noteId}/generate-quiz`).then(r => r.data.content)

export default http
```

- [ ] **Step 3: 创建 Pinia Store stores/noteStore.js**

```js
import { defineStore } from 'pinia'
import { getFolders, getNotes, getNote, createNote, updateNote, deleteNote } from '../api/index.js'

export const useNoteStore = defineStore('note', {
  state: () => ({
    folders: [],           // 所有文件夹
    notes: [],             // 笔记列表
    currentNote: null,     // 正在编辑的笔记
    selectedFolderId: null,// 当前筛选的文件夹
    searchKeyword: '',     // 搜索关键词
    chatOpen: false        // 聊天面板是否展开
  }),

  actions: {
    async loadFolders() {
      this.folders = await getFolders()
    },

    async loadNotes() {
      this.notes = await getNotes(this.selectedFolderId, this.searchKeyword)
    },

    async selectNote(id) {
      this.currentNote = await getNote(id)
    },

    async newNote(folderId = null) {
      const note = await createNote({
        title: '未命名笔记',
        content: '',
        folderId
      })
      await this.loadNotes()
      this.currentNote = note
      return note
    },

    async saveNote(id, data) {
      await updateNote(id, data)
      if (this.currentNote && this.currentNote.id === id) {
        Object.assign(this.currentNote, data)
      }
      await this.loadNotes()
    },

    async deleteCurrentNote(id) {
      await deleteNote(id)
      this.currentNote = null
      await this.loadNotes()
    },

    filterByFolder(folderId) {
      this.selectedFolderId = folderId
      this.loadNotes()
    },

    search(keyword) {
      this.searchKeyword = keyword
      this.loadNotes()
    }
  }
})
```

- [ ] **Step 4: 编译验证**

```bash
cd d:/codeProject/study/frontend && npm run dev &
sleep 5
curl -s http://localhost:5173 | head -20
```

预期：返回 HTML，无构建错误

---

### Task 9: App.vue 主布局 + main.js

**Files:**
- Modify: `frontend/src/main.js`
- Modify: `frontend/src/App.vue`
- Create: `frontend/src/components/ResizableLayout.vue`

- [ ] **Step 1: 修改 main.js — 挂载 Pinia + 全局样式**

```js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './assets/main.css'

const app = createApp(App)
app.use(createPinia())
app.mount('#app')
```

- [ ] **Step 2: 创建 ResizableLayout.vue — 三栏可拖拽布局**

```vue
<template>
  <div class="resizable-layout">
    <!-- 左栏：笔记列表 -->
    <div class="panel panel-left" :style="{ width: leftWidth + 'px' }" v-show="!leftCollapsed">
      <slot name="left"></slot>
    </div>

    <!-- 分隔条 -->
    <div class="resizer" @mousedown="startResize('left', $event)" v-show="!leftCollapsed && !centerCollapsed"></div>

    <!-- 中栏：AI 聊天 -->
    <div class="panel panel-center" :style="{ width: centerWidth + 'px' }" v-show="!centerCollapsed">
      <slot name="center"></slot>
    </div>

    <!-- 分隔条 -->
    <div class="resizer" @mousedown="startResize('center', $event)" v-show="!centerCollapsed"></div>

    <!-- 右栏：编辑器 -->
    <div class="panel panel-right" v-show="!rightCollapsed">
      <slot name="right"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const leftWidth = ref(200)
const centerWidth = ref(280)
const leftCollapsed = ref(false)
const centerCollapsed = ref(false)
const rightCollapsed = ref(false)

let dragging = null

function startResize(panel, e) {
  dragging = panel
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function onMouseMove(e) {
  if (dragging === 'left') {
    leftWidth.value = Math.max(120, Math.min(350, e.clientX))
  } else if (dragging === 'center') {
    centerWidth.value = Math.max(180, Math.min(450, e.clientX - leftWidth.value))
  }
}

function onMouseUp() {
  dragging = null
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
}

defineExpose({ leftCollapsed, centerCollapsed, rightCollapsed })
</script>

<style scoped>
.resizable-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.panel {
  overflow-y: auto;
  flex-shrink: 0;
}
.panel-left { background: var(--bg-panel); border-right: 1px solid var(--border); }
.panel-center { background: var(--bg-card); border-right: 1px solid var(--border); }
.panel-right { flex: 1; overflow-y: auto; }
.resizer {
  width: 4px;
  cursor: col-resize;
  background: transparent;
  flex-shrink: 0;
  transition: background 0.15s;
}
.resizer:hover { background: var(--primary-light); }
</style>
```

- [ ] **Step 3: 修改 App.vue — 主骨架**

```vue
<template>
  <div id="app-container">
    <TopBar
      @new-note="handleNewNote"
      @search="handleSearch"
      @toggle-left="layoutRef.leftCollapsed = !layoutRef.leftCollapsed"
      @toggle-center="layoutRef.centerCollapsed = !layoutRef.centerCollapsed"
    />
    <ResizableLayout ref="layoutRef">
      <template #left>
        <NoteListPanel
          @select="handleSelectNote"
          @delete="handleDeleteNote"
        />
      </template>
      <template #center>
        <ChatPanel v-if="store.currentNote" :note-id="store.currentNote.id" />
        <div v-else class="empty-panel">
          <p>选择一篇笔记开始 AI 对话</p>
        </div>
      </template>
      <template #right>
        <EditorPanel v-if="store.currentNote" />
        <div v-else class="empty-panel">
          <p>选择或新建一篇笔记</p>
        </div>
      </template>
    </ResizableLayout>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useNoteStore } from './stores/noteStore.js'
import ResizableLayout from './components/ResizableLayout.vue'
import TopBar from './components/TopBar.vue'
import NoteListPanel from './components/NoteListPanel.vue'
import ChatPanel from './components/ChatPanel.vue'
import EditorPanel from './components/EditorPanel.vue'

const store = useNoteStore()
const layoutRef = ref(null)

onMounted(async () => {
  await store.loadFolders()
  await store.loadNotes()
})

async function handleNewNote() {
  await store.newNote(store.selectedFolderId)
}

function handleSelectNote(id) {
  store.selectNote(id)
}

async function handleDeleteNote(id) {
  if (confirm('确定删除这篇笔记吗？')) {
    await store.deleteCurrentNote(id)
  }
}

function handleSearch(keyword) {
  store.search(keyword)
}
</script>

<style scoped>
#app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  font-size: 14px;
}
</style>
```

- [ ] **Step 4: 验证前端启动无报错**

```bash
cd d:/codeProject/study/frontend && npm run dev &
```

浏览器打开 `http://localhost:5173`，确认页面出现无报错（组件尚未创建，会有 import 错误属于预期）

---

### Task 10: TopBar 组件

**Files:**
- Create: `frontend/src/components/TopBar.vue`

- [ ] **Step 1: 创建 TopBar.vue**

```vue
<template>
  <div class="topbar">
    <div class="topbar-left">
      <div class="logo">N</div>
      <span class="title">我的学习笔记</span>
    </div>
    <div class="topbar-center">
      <input
        class="input search-input"
        placeholder="搜索笔记..."
        v-model="keyword"
        @input="emit('search', keyword)"
      />
    </div>
    <div class="topbar-right">
      <button class="btn" @click="emit('toggle-left')" title="切换笔记列表">📋</button>
      <button class="btn" @click="emit('toggle-center')" title="切换AI面板">💬</button>
      <button class="btn btn-primary" @click="emit('new-note')">+ 新建笔记</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const emit = defineEmits(['new-note', 'search', 'toggle-left', 'toggle-center'])
const keyword = ref('')
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 16px;
  background: white;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: bold;
  flex-shrink: 0;
}
.title {
  font-weight: bold;
  color: var(--primary-dark);
  font-size: 16px;
  white-space: nowrap;
}
.topbar-center {
  flex: 1;
  max-width: 300px;
}
.search-input {
  font-size: 12px;
  padding: 6px 12px;
}
.topbar-right {
  display: flex;
  gap: 6px;
  align-items: center;
}
</style>
```

- [ ] **Step 2: 验证编译**

```bash
cd d:/codeProject/study/frontend && npx vite build --mode development 2>&1 | tail -5
```

---

### Task 11: NoteListPanel 组件

**Files:**
- Create: `frontend/src/components/NoteListPanel.vue`
- Create: `frontend/src/components/NoteListItem.vue`

- [ ] **Step 1: 创建 NoteListItem.vue**

```vue
<template>
  <div class="note-item" :class="{ active: active }" @click="emit('select', note.id)">
    <div class="note-item-title">{{ note.title }}</div>
    <div class="note-item-folder">📁 {{ folderName }}</div>
    <div class="note-item-time">{{ formatTime(note.updatedAt || note.createdAt) }}</div>
    <button class="note-item-delete" @click.stop="emit('delete', note.id)">×</button>
  </div>
</template>

<script setup>
const props = defineProps({
  note: Object,
  active: Boolean,
  folderName: { type: String, default: '未分类' }
})
const emit = defineEmits(['select', 'delete'])

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth()+1}-${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.note-item {
  padding: 10px 12px;
  border-radius: var(--radius);
  cursor: pointer;
  position: relative;
  background: white;
  margin-bottom: 4px;
  transition: all 0.15s;
}
.note-item:hover { background: #F0ECF8; }
.note-item.active {
  border: 1.5px solid var(--primary-light);
  box-shadow: 0 1px 4px rgba(100,80,160,0.08);
}
.note-item-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--primary-dark);
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.note-item-folder {
  font-size: 10px;
  color: var(--text-muted);
  font-family: var(--font-sans);
  margin-bottom: 2px;
}
.note-item-time {
  font-size: 9px;
  color: #CCC;
  font-family: var(--font-sans);
}
.note-item-delete {
  position: absolute;
  right: 6px;
  top: 6px;
  background: none;
  border: none;
  color: #CCC;
  font-size: 16px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.15s;
}
.note-item:hover .note-item-delete { opacity: 1; }
.note-item-delete:hover { color: #E88; }
</style>
```

- [ ] **Step 2: 创建 NoteListPanel.vue**

```vue
<template>
  <div class="note-list-panel">
    <!-- 文件夹选择 -->
    <div class="panel-section">
      <select class="input folder-select" v-model="folderFilter" @change="onFolderChange">
        <option :value="null">📁 全部笔记</option>
        <option v-for="f in store.folders" :key="f.id" :value="f.id">📁 {{ f.name }}</option>
      </select>
    </div>

    <!-- 新建文件夹 -->
    <div class="panel-section folder-create">
      <input class="input" v-model="newFolderName" placeholder="新建文件夹..." @keyup.enter="addFolder" />
    </div>

    <!-- 笔记列表 -->
    <div class="note-list">
      <NoteListItem
        v-for="note in store.notes"
        :key="note.id"
        :note="note"
        :active="store.currentNote?.id === note.id"
        :folder-name="getFolderName(note.folderId)"
        @select="handleSelect"
        @delete="handleDelete"
      />
      <div v-if="store.notes.length === 0" class="empty-notes">
        暂无笔记，点击右上角 + 新建
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useNoteStore } from '../stores/noteStore.js'
import { createFolder } from '../api/index.js'
import NoteListItem from './NoteListItem.vue'

const store = useNoteStore()
const emit = defineEmits(['select', 'delete'])

const folderFilter = ref(null)
const newFolderName = ref('')

function onFolderChange() {
  store.filterByFolder(folderFilter.value)
}

async function addFolder() {
  const name = newFolderName.value.trim()
  if (!name) return
  await createFolder(name)
  await store.loadFolders()
  newFolderName.value = ''
}

function getFolderName(folderId) {
  const f = store.folders.find(x => x.id === folderId)
  return f ? f.name : '未分类'
}

function handleSelect(id) { emit('select', id) }
function handleDelete(id) { emit('delete', id) }
</script>

<style scoped>
.note-list-panel {
  padding: 12px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.panel-section { margin-bottom: 10px; }
.folder-select { font-size: 12px; font-family: var(--font-sans); }
.folder-create input { font-size: 11px; padding: 5px 10px; }
.note-list { flex: 1; overflow-y: auto; }
.empty-notes {
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 40px;
}
</style>
```

- [ ] **Step 3: 验证编译**

```bash
cd d:/codeProject/study/frontend && npx vite build --mode development 2>&1 | tail -5
```

---

### Task 12: ChatPanel 组件

**Files:**
- Create: `frontend/src/components/ChatPanel.vue`
- Create: `frontend/src/components/ChatMessage.vue`
- Create: `frontend/src/components/ChatInput.vue`

- [ ] **Step 1: 创建 ChatMessage.vue**

```vue
<template>
  <div class="chat-msg" :class="message.role">
    <div class="chat-msg-bubble">{{ message.content }}</div>
  </div>
</template>

<script setup>
defineProps({ message: Object })
</script>

<style scoped>
.chat-msg { margin-bottom: 10px; display: flex; }
.chat-msg.user { justify-content: flex-end; }
.chat-msg.ai { justify-content: flex-start; }
.chat-msg-bubble {
  max-width: 85%;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.6;
  word-break: break-word;
}
.user .chat-msg-bubble {
  background: #F0ECF8;
  color: var(--primary-dark);
  border-bottom-right-radius: 3px;
}
.ai .chat-msg-bubble {
  background: linear-gradient(135deg, #E8E0FF, #F0E8FF);
  color: var(--primary-dark);
  border-bottom-left-radius: 3px;
}
</style>
```

- [ ] **Step 2: 创建 ChatInput.vue**

```vue
<template>
  <div class="chat-input-area">
    <textarea
      class="input chat-textarea"
      v-model="text"
      placeholder="输入你的问题..."
      @keydown.enter.exact.prevent="send"
      rows="2"
    ></textarea>
    <button class="btn btn-primary chat-send-btn" @click="send" :disabled="!text.trim() || sending">
      {{ sending ? '...' : '发送' }}
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const emit = defineEmits(['send'])
const props = defineProps({ sending: Boolean })

const text = ref('')

function send() {
  if (!text.value.trim() || props.sending) return
  emit('send', text.value)
  text.value = ''
}
</script>

<style scoped>
.chat-input-area {
  display: flex;
  gap: 6px;
  padding: 10px 12px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}
.chat-textarea {
  flex: 1;
  font-size: 12px;
  resize: none;
  font-family: var(--font-sans);
}
.chat-send-btn {
  align-self: flex-end;
  flex-shrink: 0;
}
</style>
```

- [ ] **Step 3: 创建 ChatPanel.vue**

```vue
<template>
  <div class="chat-panel">
    <div class="chat-header">
      ✨ 讨论「{{ store.currentNote?.title || '笔记' }}」
      <button class="btn" style="margin-left:auto;font-size:10px" @click="generateNoteFromChat">📝 生成笔记</button>
    </div>

    <div class="chat-messages" ref="msgList">
      <ChatMessage v-for="msg in messages" :key="msg.id" :message="msg" />
      <!-- 流式输出中的临时消息 -->
      <div v-if="streamingText" class="chat-msg ai">
        <div class="chat-msg-bubble streaming">{{ streamingText }}</div>
      </div>
      <div v-if="messages.length === 0 && !streamingText" class="chat-empty">
        开始和 AI 讨论这篇笔记吧
      </div>
    </div>

    <ChatInput @send="handleSend" :sending="isStreaming" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useNoteStore } from '../stores/noteStore.js'
import { getMessages, chatStream, generateNote, updateNote } from '../api/index.js'
import ChatMessage from './ChatMessage.vue'
import ChatInput from './ChatInput.vue'

const props = defineProps({ noteId: Number })
const store = useNoteStore()
const messages = ref([])
const streamingText = ref('')
const isStreaming = ref(false)
const msgList = ref(null)

onMounted(() => loadMessages())
watch(() => props.noteId, () => { if (props.noteId) loadMessages() })

async function loadMessages() {
  if (!props.noteId) return
  messages.value = await getMessages(props.noteId)
  await nextTick()
  scrollBottom()
}

function handleSend(text) {
  isStreaming.value = true
  streamingText.value = ''
  messages.value.push({
    id: Date.now(),
    noteId: props.noteId,
    role: 'user',
    content: text
  })
  nextTick(() => scrollBottom())

  chatStream(
    props.noteId, text,
    (chunk) => { streamingText.value += chunk; nextTick(() => scrollBottom()) },
    () => {
      messages.value.push({
        id: Date.now() + 1,
        noteId: props.noteId,
        role: 'ai',
        content: streamingText.value
      })
      streamingText.value = ''
      isStreaming.value = false
    },
    (err) => {
      streamingText.value = '[发送失败: ' + err.message + ']'
      isStreaming.value = false
    }
  )
}

async function generateNoteFromChat() {
  const content = await generateNote(props.noteId)
  if (content) {
    await updateNote(props.noteId, { content })
    store.currentNote.content = content
  }
}

function scrollBottom() {
  if (msgList.value) msgList.value.scrollTop = msgList.value.scrollHeight
}
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.chat-header {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-light);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}
.chat-empty {
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 60px;
}
.streaming {
  border-left: 2px solid var(--primary-light);
  animation: pulse 1s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
```

- [ ] **Step 4: 验证编译**

```bash
cd d:/codeProject/study/frontend && npx vite build --mode development 2>&1 | tail -5
```

---

### Task 13: EditorPanel 组件

**Files:**
- Create: `frontend/src/components/EditorPanel.vue`
- Create: `frontend/src/components/MarkdownToolbar.vue`

- [ ] **Step 1: 创建 MarkdownToolbar.vue**

```vue
<template>
  <div class="md-toolbar">
    <button class="md-btn" @click="insert('**', '**')" title="加粗"><b>B</b></button>
    <button class="md-btn" @click="insert('*', '*')" title="斜体"><i>I</i></button>
    <button class="md-btn" @click="insertLine('# ')" title="一级标题">H1</button>
    <button class="md-btn" @click="insertLine('## ')" title="二级标题">H2</button>
    <button class="md-btn" @click="insertLine('### ')" title="三级标题">H3</button>
    <button class="md-btn" @click="insert('~~', '~~')" title="删除线"><s>S</s></button>
    <button class="md-btn" @click="insert('==', '==')" title="荧光笔">🖍</button>
    <button class="md-btn" @click="insertLine('- [ ] ')" title="待办">☐</button>
    <button class="md-btn" @click="insertLine('1. ')" title="有序列表">1.</button>
    <button class="md-btn" @click="insert('[', '](url)')" title="链接">🔗</button>
    <button class="md-btn" @click="insert('![alt](', ')')" title="图片">🖼</button>
    <button class="md-btn" @click="insert('`', '`')" title="行内代码">&lt;/&gt;</button>
    <button class="md-btn" @click="insertLine('> ')" title="引用">❝</button>
    <span style="flex:1"></span>
    <button
      class="md-btn"
      :class="{ active: mode === 'edit' }"
      @click="mode = 'edit'"
    >源码</button>
    <button
      class="md-btn"
      :class="{ active: mode === 'preview' }"
      @click="mode = 'preview'"
    >预览</button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const mode = ref('edit')
const emit = defineEmits(['insert', 'insertLine', 'modeChange'])

watch(mode, (val) => emit('modeChange', val))

function insert(before, after) {
  emit('insert', before, after)
}

function insertLine(prefix) {
  emit('insertLine', prefix)
}
</script>

<style scoped>
.md-toolbar {
  display: flex;
  gap: 2px;
  flex-wrap: wrap;
  padding: 6px 8px;
  background: #FAFAFA;
  border-radius: 6px;
  border: 1px solid #EEE;
  margin-bottom: 12px;
}
.md-btn {
  border: 1px solid #DDD;
  background: white;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  cursor: pointer;
  font-family: var(--font-sans);
  color: var(--text);
  min-width: 28px;
}
.md-btn:hover { background: #F0ECF8; }
.md-btn.active { background: var(--primary-light); color: white; border-color: var(--primary); }
</style>
```

- [ ] **Step 2: 创建 EditorPanel.vue**

```vue
<template>
  <div class="editor-panel" v-if="store.currentNote">
    <!-- 笔记标题 -->
    <input
      class="note-title-input"
      v-model="localTitle"
      placeholder="笔记标题..."
      @input="markDirty"
    />

    <!-- 文件夹选择 -->
    <div class="note-folder-row">
      <select class="input folder-select" v-model="localFolderId" @change="markDirty">
        <option :value="null">📁 未分类</option>
        <option v-for="f in store.folders" :key="f.id" :value="f.id">📁 {{ f.name }}</option>
      </select>
      <span class="note-time">{{ formatTime(store.currentNote.updatedAt) }}</span>
    </div>

    <!-- Markdown 工具栏 -->
    <MarkdownToolbar
      @insert="handleInsert"
      @insert-line="handleInsertLine"
      @mode-change="handleModeChange"
    />

    <!-- 编辑/预览区域 -->
    <div class="editor-area">
      <textarea
        v-show="viewMode === 'edit'"
        ref="textareaRef"
        class="editor-textarea"
        v-model="localContent"
        @input="markDirty"
      ></textarea>
      <div
        v-show="viewMode === 'preview'"
        class="editor-preview"
        v-html="renderedMarkdown"
      ></div>
    </div>

    <!-- 底部按钮 -->
    <div class="editor-footer">
      <button class="btn" @click="generateQuiz">📝 AI 出题</button>
      <div style="flex:1"></div>
      <button class="btn btn-primary" @click="save" :disabled="!isDirty">💾 保存</button>
    </div>

    <!-- AI 出题弹窗 -->
    <div v-if="quizContent" class="quiz-overlay" @click.self="quizContent = ''">
      <div class="quiz-card">
        <h3>📝 AI 自测题</h3>
        <div class="quiz-body" v-html="quizHtml"></div>
        <button class="btn" @click="quizContent = ''">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useNoteStore } from '../stores/noteStore.js'
import { updateNote, generateQuiz } from '../api/index.js'
import { marked } from 'marked'
import MarkdownToolbar from './MarkdownToolbar.vue'

const store = useNoteStore()
const textareaRef = ref(null)

const localTitle = ref('')
const localContent = ref('')
const localFolderId = ref(null)
const isDirty = ref(false)
const viewMode = ref('edit')
const quizContent = ref('')

// 当 store.currentNote 变化时，同步到本地
watch(() => store.currentNote, (note) => {
  if (note) {
    localTitle.value = note.title
    localContent.value = note.content || ''
    localFolderId.value = note.folderId
    isDirty = false
  }
}, { immediate: true })

const renderedMarkdown = computed(() => marked(localContent.value || ''))

const quizHtml = computed(() => marked(quizContent.value || ''))

function markDirty() { isDirty.value = true }

function handleInsert(before, after) {
  const ta = textareaRef.value
  if (!ta) return
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const selected = localContent.value.substring(start, end)
  const newText = localContent.value.substring(0, start) + before + selected + after + localContent.value.substring(end)
  localContent.value = newText
  markDirty()
  setTimeout(() => {
    ta.focus()
    ta.selectionStart = start + before.length
    ta.selectionEnd = start + before.length + selected.length
  }, 0)
}

function handleInsertLine(prefix) {
  const ta = textareaRef.value
  if (!ta) return
  const start = localContent.value.lastIndexOf('\n', ta.selectionStart - 1) + 1
  localContent.value = localContent.value.substring(0, start) + prefix + localContent.value.substring(start)
  markDirty()
  setTimeout(() => { ta.focus() }, 0)
}

function handleModeChange(mode) { viewMode.value = mode }

async function save() {
  await updateNote(store.currentNote.id, {
    title: localTitle.value,
    content: localContent.value,
    folderId: localFolderId.value
  })
  store.currentNote.title = localTitle.value
  store.currentNote.content = localContent.value
  store.currentNote.folderId = localFolderId.value
  isDirty.value = false
}

async function generateQuiz() {
  const quiz = await generateQuiz(store.currentNote.id)
  quizContent.value = quiz
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.editor-panel {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 800px;
}
.note-title-input {
  border: none;
  font-size: 20px;
  font-weight: bold;
  color: var(--primary-dark);
  margin-bottom: 6px;
  outline: none;
  font-family: var(--font-serif);
  width: 100%;
}
.note-folder-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.folder-select {
  width: auto;
  font-size: 11px;
  font-family: var(--font-sans);
  padding: 4px 10px;
}
.note-time {
  font-size: 10px;
  color: var(--text-muted);
  font-family: var(--font-sans);
}
.editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}
.editor-textarea {
  flex: 1;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 14px;
  font-size: 14px;
  font-family: var(--font-serif);
  color: var(--text);
  line-height: 1.7;
  resize: none;
  outline: none;
}
.editor-textarea:focus { border-color: var(--primary-light); }
.editor-preview {
  flex: 1;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 14px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.7;
}
.editor-footer {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}
/* 出题弹窗 */
.quiz-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.quiz-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  width: 90%;
}
.quiz-card h3 { margin-bottom: 16px; color: var(--primary-dark); }
.quiz-body { font-size: 14px; line-height: 1.8; }
</style>
```

- [ ] **Step 3: 验证完整前端构建**

```bash
cd d:/codeProject/study/frontend && npx vite build --mode development 2>&1 | tail -10
```

预期：无报错，输出 built in Xms

---

### Task 14: 前端启动 + 端到端验证

- [ ] **Step 1: 确保后端运行**

```bash
curl -s http://localhost:8080/api/notes | head -50
```

- [ ] **Step 2: 启动前端**

```bash
cd d:/codeProject/study/frontend && npm run dev &
sleep 5
```

- [ ] **Step 3: 浏览器打开 http://localhost:5173 手动验证**

验证清单：
- [ ] 页面正常显示，三栏布局可见
- [ ] 顶部栏有 logo、搜索框、+按钮
- [ ] 左侧可以新建文件夹、筛选文件夹
- [ ] 点击 + 新建笔记，右侧出现编辑器
- [ ] Markdown 工具栏按钮可点击，源码/预览可切换
- [ ] 中间 AI 面板可以发送消息（需 DeepSeek API Key 配置）
- [ ] 拖拽分隔条可以调整面板宽度
- [ ] 笔记保存成功，刷新后仍在
- [ ] 搜索笔记功能正常

---

## 阶段三：验证与收尾

### Task 15: 后端单元测试

**Files:**
- Create: `backend/src/test/java/com/studynote/NoteServiceTest.java`

- [ ] **Step 1: 创建 NoteService 测试**

```java
package com.studynote;

import com.studynote.entity.Note;
import com.studynote.mapper.NoteMapper;
import com.studynote.service.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteMapper noteMapper;

    @Test
    void testCreateAndList() {
        Note note = noteService.create("测试笔记", "这是内容", null);
        assertThat(note.getId()).isNotNull();
        assertThat(note.getTitle()).isEqualTo("测试笔记");

        List<Note> notes = noteService.list(null, null);
        assertThat(notes).isNotEmpty();
    }

    @Test
    void testSearch() {
        noteService.create("Spring Boot入门", "学习Spring框架基础", null);
        List<Note> result = noteService.list(null, "Spring");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTitle()).contains("Spring");
    }

    @Test
    void testUpdate() {
        Note note = noteService.create("原始标题", "原始内容", null);
        noteService.update(note.getId(), "新标题", "新内容", null);
        Note updated = noteService.getById(note.getId());
        assertThat(updated.getTitle()).isEqualTo("新标题");
    }

    @Test
    void testDelete() {
        Note note = noteService.create("待删笔记", "内容", null);
        noteService.delete(note.getId());
        Note deleted = noteService.getById(note.getId());
        assertThat(deleted).isNull();
    }
}
```

- [ ] **Step 2: 运行测试**

```bash
cd d:/codeProject/study/backend && mvn test -q
```

预期：Tests run: 4, Failures: 0, Errors: 0

---

### Task 16: 最终检查清单

- [ ] 后端所有接口 curl 验证通过
- [ ] 前端 npm run build 无报错
- [ ] 浏览器端到端操作：新建笔记 → 编辑 → 保存 → 搜索 → 删除 全流程正常
- [ ] 聊天面板（需配置 DeepSeek API Key）：发送消息 → 流式返回 → 生成笔记 ✅
- [ ] 面板拖拽、收起正常
- [ ] 响应式/移动端？（本项目不做移动端适配，仅桌面端）

---
