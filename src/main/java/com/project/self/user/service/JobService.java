package com.project.self.user.service;

import com.project.self.user.dto.JobDescriptionParseResponse;
import com.project.self.user.dto.ResumeParseResponse;
import com.project.self.user.entity.Job;
import com.project.self.user.entity.Skill;
import com.project.self.user.entity.User;
import com.project.self.user.repository.JobRepository;
import com.project.self.user.repository.SkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SkillService skillService;

    private static final double MANDATORY_WEIGHT = 0.7;
    private static final double OPTIONAL_WEIGHT = 0.3;

    private final WebClient webClient = WebClient.create("http://localhost:8000");

    public Job postJob(Job job) {
        JobDescriptionParseResponse response = webClient.post()
                .uri("/parse-job-description")
                .bodyValue(Map.of("text", job.getDescription()))
                .retrieve()
                .bodyToMono(JobDescriptionParseResponse.class)
                .block();
        for (String skillName : response.getMandatorySkills()) {
            Skill skill = skillService.getOrCreateSkill(skillName);
            job.getMandatorySkills().add(skill);
        }
        for (String skillName : response.getOptionalSkills()) {
            Skill skill = skillService.getOrCreateSkill(skillName);
            job.getOptionalSkills().add(skill);
        }

        return jobRepository.save(job);
    }

    public List<Job> getAllJobs(User applicant) {

        List<Job> jobs = jobRepository.findAll();
        log.info("user name: " + applicant.getUsername());
        log.info("user skills: " + applicant.getSkills());

        jobs.forEach(job -> {
            double score = calculateWeightedScore(
                    applicant.getSkills(),
                    job
            );
            job.setScore(score);
        });
        return jobs.stream()
                .sorted(Comparator.comparingDouble(Job::getScore).reversed())
                .toList();

    }

    public double calculateWeightedScore(Set<Skill> skills, Job job) {

        Set<UUID> candidateSkillIds = skills.stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());

        double mandatoryScore = calculateCategoryScore(
                candidateSkillIds,
                job.getMandatorySkills()
        );

        double optionalScore = calculateCategoryScore(
                candidateSkillIds,
                job.getOptionalSkills()
        );

        return (mandatoryScore * MANDATORY_WEIGHT
                + optionalScore * OPTIONAL_WEIGHT) * 100;
    }

    private double calculateCategoryScore(Set<UUID> candidateSkillIds,
                                          Set<Skill> jobSkills) {

        if (jobSkills == null || jobSkills.isEmpty()) {
            return 0.0;
        }

        long matched = jobSkills.stream()
                .map(Skill::getId)
                .filter(candidateSkillIds::contains)
                .count();

        return (double) matched / jobSkills.size();
    }

    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }
}