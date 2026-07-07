package com.seob.chat.service;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, Advisor[] advisors) {
        this.chatClient = chatClientBuilder.defaultAdvisors(advisors).build();
    }

    public Flux<String> stream(Prompt prompt, String conversationId) {
        return prepareRequest(prompt, conversationId)
                .stream()
                .content();
    }

    public @Nullable ChatResponse call(Prompt prompt, String conversationId) {
        return prepareRequest(prompt, conversationId)
                .call()
                .chatResponse();
    }

    private ChatClient.ChatClientRequestSpec prepareRequest(Prompt prompt, String conversationId) {
        return chatClient.prompt(prompt)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId));
    }

    // 1. 긴급도, 문의 카테고리 Enum 정의
    public enum Urgency { LOW, NORMAL, HIGH, URGENT }
    public enum Category { REFUND, SHIPPING, DEFECT, INQUIRY }

    // 2. 응답 레코드 정의
    public record CsEvaluation(
            Category category,
            Urgency urgency,
            List<String> keywords // 예: ["배송지연", "환불요청", "파손"]
    ) {}

    public CsEvaluation csEvaluation(Prompt prompt, String conversationId) {
        return prepareRequest(prompt, conversationId).call().entity(CsEvaluation.class);
    }
}
