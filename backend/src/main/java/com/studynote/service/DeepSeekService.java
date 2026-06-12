package com.studynote.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class DeepSeekService {
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(30))
        .readTimeout(Duration.ofSeconds(180))
        .writeTimeout(Duration.ofSeconds(30))
        .build();
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
