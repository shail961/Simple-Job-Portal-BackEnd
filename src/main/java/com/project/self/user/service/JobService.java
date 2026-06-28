package com.project.self.user.service;

import com.project.self.user.ai.dto.JobSummary;
import com.project.self.user.dto.JobDescriptionParseResponse;
import com.project.self.user.dto.RagExplainRequest;
import com.project.self.user.dto.RagExplainResponse;
import com.project.self.user.entity.Job;
import com.project.self.user.entity.Skill;
import com.project.self.user.entity.User;
import com.project.self.user.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public RagExplainResponse explainMatchScore(Job job, User applicant) {
        double mandatoryScore = calculateCategoryScore(
                applicant.getSkills().stream().map(Skill::getId).collect(Collectors.toSet()),
                job.getMandatorySkills()
        );

        double optionalScore = calculateCategoryScore(
                applicant.getSkills().stream().map(Skill::getId).collect(Collectors.toSet()),
                job.getOptionalSkills()
        );

        RagExplainRequest request = RagExplainRequest.builder()
                .jobTitle(job.getTitle())
                .jobDescription(job.getDescription())
                .mandatorySkills(job.getMandatorySkills().stream().map(Skill::getName).toList())
                .optionalSkills(job.getOptionalSkills().stream().map(Skill::getName).toList())
                .candidateSkills(applicant.getSkills().stream().map(Skill::getName).toList())
//                .resumeText(applicant.getResumeText())
                .userId(applicant.getId())
                .mandatoryMatch(mandatoryScore)
                .optionalMatch(optionalScore)
                .weightedScore((mandatoryScore * MANDATORY_WEIGHT
                        + optionalScore * OPTIONAL_WEIGHT) * 100)
                .build();
        log.info("request: {}", request);

        var response = webClient.post()
                .uri("/rag/explain")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RagExplainResponse.class)
                .block();
        log.info("response: {}", response);
        return response;
    }

    public List<JobSummary> searchJobs(
            String keyword,
            String city,
            List<String> skills
    ) {

        List<Job> jobs = jobRepository.findAll();

        return jobs.stream()
                .filter(job -> matchesKeyword(job, keyword))
                .filter(job -> matchesCity(job, city))
                .filter(job -> matchesSkills(job, skills))
                .map(this::toSummary)
                .toList();
    }

    private boolean matchesKeyword(
            Job job,
            String keyword
    ) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String search = keyword.toLowerCase();

        return Optional.ofNullable(job.getTitle())
                .orElse("")
                .toLowerCase()
                .contains(search)

                ||

                Optional.ofNullable(job.getDescription())
                        .orElse("")
                        .toLowerCase()
                        .contains(search);
    }

    private boolean matchesCity(
            Job job,
            String city
    ) {

        if (city == null || city.isBlank()) {
            return true;
        }

        return city.equalsIgnoreCase(
                Optional.ofNullable(job.getLocation())
                        .orElse("")
        );
    }

    private boolean matchesSkills(
            Job job,
            List<String> skills
    ) {

        if (skills == null || skills.isEmpty()) {
            return true;
        }

        Set<String> jobSkills = new HashSet<>();

        if (job.getMandatorySkills() != null) {
            job.getMandatorySkills()
                    .stream()
                    .map(Skill::getName)
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .forEach(jobSkills::add);
        }

        if (job.getOptionalSkills() != null) {
            job.getOptionalSkills()
                    .stream()
                    .map(Skill::getName)
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .forEach(jobSkills::add);
        }

        return skills.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .allMatch(jobSkills::contains);
    }

    private JobSummary toSummary(
            Job job
    ) {

        return new JobSummary(

                job.getId(),

                job.getTitle(),

                job.getLocation(),

                job.getDescription(),

                Optional.ofNullable(job.getMandatorySkills())
                        .orElse(Set.of())
                        .stream()
                        .map(Skill::getName)
                        .toList(),

                Optional.ofNullable(job.getOptionalSkills())
                        .orElse(Set.of())
                        .stream()
                        .map(Skill::getName)
                        .toList()

        );
    }
}