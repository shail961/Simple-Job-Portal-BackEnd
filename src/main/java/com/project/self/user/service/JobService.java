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

    private final WebClient webClient = WebClient.create("http://localhost:8000");

    public Job postJob(Job job) {
        JobDescriptionParseResponse response = webClient.post()
                .uri("/parse-job-description")
                .bodyValue(Map.of("text", job.getDescription()))
                .retrieve()
                .bodyToMono(JobDescriptionParseResponse.class)
                .block();
        for (String skillName : response.getSkills()) {
            Skill skill = skillService.getOrCreateSkill(skillName);
            job.getRequiredSkills().add(skill);
        }

        return jobRepository.save(job);
    }

    public List<Job> getAllJobs(User applicant) {

        List<Job> jobs = jobRepository.findAll();
        log.info("user name: " + applicant.getUsername());
        log.info("user skills: " + applicant.getSkills());

        jobs.forEach(job -> {
            double score = calculateMatchScore(
                    applicant.getSkills(),
                    job.getRequiredSkills()
            );
            job.setScore(score);
        });
        return jobs.stream()
                .sorted(Comparator.comparingDouble(Job::getScore).reversed())
                .toList();

    }

    private double calculateMatchScore(Set<Skill> candidateSkills,
                                       Set<Skill> jobSkills) {

        if (jobSkills == null || jobSkills.isEmpty()) {
            return 0.0;
        }

        Set<UUID> candidateSkillIds = candidateSkills.stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());

        long matchedCount = jobSkills.stream()
                .map(Skill::getId)
                .filter(candidateSkillIds::contains)
                .count();

        return (double) matchedCount / jobSkills.size() * 100;
    }

    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }
}