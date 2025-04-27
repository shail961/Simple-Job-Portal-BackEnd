package com.project.self.user.service;

import com.project.self.user.entity.Job;
import com.project.self.user.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;

    public Job postJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }
}