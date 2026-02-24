package com.project.self.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JobDescriptionParseResponse {

    private List<String> mandatorySkills;
    private List<String> optionalSkills;

}
