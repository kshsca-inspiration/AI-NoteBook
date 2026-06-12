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

    /** 根据聊天历史生成笔记（追加模式） */
    public String generateNote(Long noteId) {
        Note note = noteService.getById(noteId);
        List<ChatMessage> messages = getMessages(noteId);
        StringBuilder chatLog = new StringBuilder();
        for (ChatMessage msg : messages) {
            chatLog.append(msg.getRole().equals("user") ? "用户" : "AI").append("：")
                   .append(msg.getContent()).append("\n\n");
        }
        String existingContent = note != null && note.getContent() != null ? note.getContent() : "";
        String prompt = "以下是用户和AI的学习讨论记录，请基于讨论内容生成补充笔记（Markdown格式），" +
            "输出新增的要点和知识归纳，如果当前笔记已有相关内容就跳过不要重复。" +
            "保留关键代码和术语：\n\n" + chatLog.toString() +
            "\n当前笔记已有内容（不要重复）：\n" + existingContent;
        return deepSeekService.chat(
            "你是学习笔记整理助手。只输出新增的Markdown内容，不要重复已有内容，不要额外解释。", prompt);
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
