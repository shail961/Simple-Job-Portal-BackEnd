package com.project.self.user.controller;

import com.project.self.user.dto.ApplicationRequest;
import com.project.self.user.entity.Application;
import com.project.self.user.entity.Job;
import com.project.self.user.entity.User;
import com.project.self.user.enums.ApplicationStatus;
import com.project.self.user.repository.JobRepository;
import com.project.self.user.service.ApplicationService;
import com.project.self.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> applyToJob(@RequestBody ApplicationRequest request, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();

        Job job = jobRepository.findById(request.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));



        Application application = new Application();
        application.setApplicant(user);
        application.setJob(job);
        application.setResumePath(request.getResumePath());
        application.setStatus(ApplicationStatus.PENDING);

        return ResponseEntity.ok(applicationService.applyToJob(application));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam ApplicationStatus status) {
        Application application = applicationService.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        return ResponseEntity.ok(applicationService.updateStatus(application, status));
    }

    @GetMapping("/by-job/{jobId}")
    public ResponseEntity<?> getApplicationsByJob(@PathVariable Long jobId, Principal principal) {
        User recruiter = userService.findByUsername(principal.getName()).orElseThrow();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view applications for this job.");
        }

        return ResponseEntity.ok(applicationService.getApplicationsByJob(job));
    }
}