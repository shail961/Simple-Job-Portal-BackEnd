package com.project.self.user.ai.dto;

import com.project.self.user.ai.payload.AgentPayload;

import java.util.List;

public record ChatResponse(

        String message,

        List<AgentPayload<?>> payloads,

        ChatMetadata metadata

) {
}