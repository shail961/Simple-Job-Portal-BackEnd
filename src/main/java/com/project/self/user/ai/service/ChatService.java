package com.project.self.user.ai.service;

import com.project.self.user.ai.context.AgentContext;
import com.project.self.user.ai.context.AgentContextHolder;
import com.project.self.user.ai.dto.ChatRequest;
import com.project.self.user.ai.dto.ChatResponse;
import com.project.self.user.ai.response.ChatResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ChatResponseBuilder chatResponseBuilder;

    public ChatResponse chat(ChatRequest request) {

        AgentContext context = new AgentContext();

        AgentContextHolder.set(context);

        try {

            String response =
                    chatClient
                            .prompt(request.message())
                            .call()
                            .content();

            return chatResponseBuilder.build(
                    response,
                    context
            );

        } finally {

            AgentContextHolder.clear();

        }
    }
}