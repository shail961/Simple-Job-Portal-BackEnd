package com.project.self.user.ai.context;

public final class AgentContextHolder {

    private static final ThreadLocal<AgentContext> HOLDER = new ThreadLocal<>();

    private AgentContextHolder() {
    }

    public static void set(AgentContext context) {
        HOLDER.set(context);
    }

    public static AgentContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}