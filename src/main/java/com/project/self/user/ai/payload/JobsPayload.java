package com.project.self.user.ai.payload;

import com.project.self.user.ai.dto.JobSummary;

import java.util.List;

public record JobsPayload(

        List<JobSummary> jobs

) {
}