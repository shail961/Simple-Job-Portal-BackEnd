package com.project.self.user.ai.dto;

import java.util.List;

public record JobSummary(
        Long id,
        String title,
        String company,
        String city,
        List<String> mandatorySkills,
        List<String> optionalSkills
) {
}