package com.project.self.user.controller;

import com.project.self.user.dto.JobRequest;
import com.project.self.user.entity.Job;
import com.project.self.user.entity.User;
import com.project.self.user.enums.Role;
import com.project.self.user.service.JobService;
import com.project.self.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> postJob(@RequestBody JobRequest request, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        if (user.getRole() != Role.RECRUITER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only recruiters can post jobs.");
        }

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setPostedBy(user);

        return ResponseEntity.ok(jobService.postJob(job));
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody JobRequest request, Principal principal) {
        User recruiter = userService.findByUsername(principal.getName()).orElseThrow();
        Job job = jobService.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this job.");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());

        return ResponseEntity.ok(jobService.updateJob(job));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJobById(@PathVariable Long jobId) {
        Optional<Job> jobOptional = jobService.findById(jobId);
        return jobOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}