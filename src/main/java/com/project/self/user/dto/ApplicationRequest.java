package com.project.self.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ApplicationRequest {
    private Long jobId;
    private MultipartFile resume;
}