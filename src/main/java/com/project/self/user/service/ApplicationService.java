package com.project.self.user.service;

import com.project.self.user.entity.Application;
import com.project.self.user.entity.Job;
import com.project.self.user.enums.ApplicationStatus;
import com.project.self.user.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    @Autowired
    private ApplicationRepository applicationRepository;

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
}
