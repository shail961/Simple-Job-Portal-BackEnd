package com.project.self.user.ai.payload;

import com.project.self.user.ai.enums.PayloadType;

public record AgentPayload<T>(

        PayloadType type,

        T data

) {
}