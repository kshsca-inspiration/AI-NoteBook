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
