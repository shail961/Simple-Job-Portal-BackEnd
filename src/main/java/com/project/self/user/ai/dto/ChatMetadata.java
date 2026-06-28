package com.project.self.user.ai.dto;

import java.util.List;

public record ChatMetadata(

        List<String> toolsUsed,

        long executionTimeMs

) {
}