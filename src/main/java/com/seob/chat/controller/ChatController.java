package com.seob.chat.controller;

import com.seob.chat.service.ChatService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.DefaultChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    public record PromptBody(@NotEmpty String conversationId,
                             @NotEmpty String userPrompt,
                             @Nullable String systemPrompt,
                             DefaultChatOptions chatOptions){}

    @PostMapping(value = "/call", produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse call(@RequestBody @Valid PromptBody promptBody) {
        Prompt prompt = createPrompt(promptBody);

        // 완성된 프롬프트와 conversationID를 가지고 서비스 메서드 호출
        return chatService.call(prompt, promptBody.conversationId());
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody @Valid PromptBody promptBody) {
        Prompt prompt = createPrompt(promptBody);
        return chatService.stream(prompt, promptBody.conversationId());
    }

    private static Prompt createPrompt(PromptBody promptBody) {
        // 1. 메시지들을 차곡차곡 담을 빈 리스트를 생성
        List<Message> messages = new ArrayList<>();

        // 2. systemPrompt가 입력으로 들어왔다면 리스트에 넣음
        if(promptBody.systemPrompt() != null && !promptBody.systemPrompt().isBlank()) {
            messages.add(new SystemMessage(promptBody.systemPrompt()));
        }

        // 3. userPrompt는 필수 값이니 무조건 리스트에 넣음
        messages.add(new UserMessage(promptBody.userPrompt()));

        // 4. 리스트에 담긴 메시지들로 프롬프트 조립
        Prompt.Builder promptBuilder = Prompt.builder().messages(messages);

        // 5. 프론트엔드에서 보낸 chatOptions가 있다면 적용
        if(promptBody.chatOptions() != null) {
            promptBuilder.chatOptions(promptBody.chatOptions());
        }
        return promptBuilder.build();
    }
}
