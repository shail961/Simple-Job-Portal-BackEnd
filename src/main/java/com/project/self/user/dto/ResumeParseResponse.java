package com.project.self.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResumeParseResponse {

    private List<String> skills;
    private String resumeText;
}