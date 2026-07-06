package com.seob.chat.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
