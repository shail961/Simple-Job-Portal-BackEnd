package com.project.self.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class RagExplainRequest {

    private double weightedScore;
    private double mandatoryMatch;
    private double optionalMatch;

    private String jobTitle;
    private String jobDescription;
    private List<String> mandatorySkills;
    private List<String> optionalSkills;
    private List<String> candidateSkills;

    private Long userId;

    private String resumeText;
}