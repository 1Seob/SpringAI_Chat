package com.seob.chat.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai")
    public String generation(String userPrompt) {
        return this.chatClient.prompt()
                .user(userPrompt)
                .call()
                .content(); // 받아온 응답 중 메타데이터는 버리고, 순수 content만 추출
    }

    // 응답이 모이는 대로 하나씩 방출하는 Flux 스트림 객체 반환
    @GetMapping(value = "/st", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(String userPrompt) {
        return chatClient.prompt()
                .user(userPrompt)
                .stream()
                .content();
    }
}
