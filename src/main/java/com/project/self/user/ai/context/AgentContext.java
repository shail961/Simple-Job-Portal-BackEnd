package com.project.self.user.ai.context;

import com.project.self.user.ai.enums.PayloadType;
import com.project.self.user.ai.payload.AgentPayload;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
public class AgentContext {

    private final List<AgentPayload<?>> payloads = new ArrayList<>();

    private final List<String> toolsUsed = new ArrayList<>();

    private long startTime;

    public AgentContext() {
        this.startTime = System.currentTimeMillis();
    }

    public void addPayload(AgentPayload<?> payload) {
        payloads.add(payload);
    }

    public void addTool(String toolName) {
        toolsUsed.add(toolName);
    }

    // return copy of the context to prevent modification
    public List<AgentPayload<?>> getPayloads() {
        return List.copyOf(payloads);
    }

    public List<String> getToolsUsed() {
        return List.copyOf(toolsUsed);
    }

    public long executionTime() {
        return System.currentTimeMillis() - startTime;
    }

    public <T> void addPayload(
            PayloadType type,
            T payload
    ) {

        payloads.add(
                new AgentPayload<>(
                        type,
                        payload
                )
        );

    }
}