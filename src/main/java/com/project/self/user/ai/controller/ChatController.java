package com.project.self.user.ai.controller;

import com.project.self.user.ai.dto.ChatRequest;
import com.project.self.user.ai.dto.ChatResponse;
import com.project.self.user.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestBody ChatRequest request
    ) {
        return chatService.chat(request);
    }
}