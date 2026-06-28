package com.project.self.user.ai.config;

import com.project.self.user.ai.tool.JobTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration {

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            JobTools jobTools
    ) {

        return builder
                .defaultTools(jobTools)
                .build();
    }

}