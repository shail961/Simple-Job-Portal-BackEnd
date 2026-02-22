package com.project.self.user.service;

import com.project.self.user.dto.ResumeParseResponse;
import com.project.self.user.entity.Application;
import com.project.self.user.entity.Job;
import com.project.self.user.entity.Skill;
import com.project.self.user.entity.User;
import com.project.self.user.enums.ApplicationStatus;
import com.project.self.user.repository.ApplicationRepository;
import com.project.self.user.repository.SkillRepository;
import com.project.self.user.repository.UserRepository;
import com.project.self.user.util.SkillNormalizer;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ApplicationService {
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SkillService skillService;

    public Application applyToJob(Application application) {
        return applicationRepository.save(application);
    }

    public Optional<Application> findById(Long id) {
        return applicationRepository.findById(id);
    }

    public Application updateStatus(Application application, ApplicationStatus status) {
        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public List<Application> getApplicationsByJob(Job job) {
        return applicationRepository.findByJob(job);
    }

    public List<Application> findByJobIn(List<Job> jobs) {
        return applicationRepository.findByJobIn(jobs);
    }

    public List<Application> findByApplicant(User applicant) {
        return applicationRepository.findByApplicant(applicant);
    }

    private final WebClient webClient = WebClient.create("http://localhost:8000");

    public ResumeParseResponse parseResume(MultipartFile file) {

        return webClient.post()
                .uri("/parse-resume")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file.getResource()))
                .retrieve()
                .bodyToMono(ResumeParseResponse.class)
                .block();
    }

    public ResumeParseResponse processResume(MultipartFile file, User applicant) throws Exception {

        ResumeParseResponse response = parseResume(file);

        for (String skillName : response.getSkills()) {
            Skill skill = skillService.getOrCreateSkill(skillName);
            applicant.getSkills().add(skill);
        }
        userService.save(applicant);

        return response;

    }


}
