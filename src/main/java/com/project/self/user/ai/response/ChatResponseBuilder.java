package com.project.self.user.ai.response;

import com.project.self.user.ai.context.AgentContext;
import com.project.self.user.ai.dto.ChatMetadata;
import com.project.self.user.ai.dto.ChatResponse;
import org.springframework.stereotype.Component;

@Component
public class ChatResponseBuilder {

    public ChatResponse build(
            String message,
            AgentContext context
    ) {

        return new ChatResponse(

                message,

                context.getPayloads(),

                new ChatMetadata(
                        context.getToolsUsed(),
                        context.executionTime()
                )

        );
    }
}