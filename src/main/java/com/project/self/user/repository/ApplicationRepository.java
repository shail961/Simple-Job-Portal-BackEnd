package com.project.self.user.repository;

import com.project.self.user.entity.Application;
import com.project.self.user.entity.Job;
import com.project.self.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJob(Job job);
    List<Application> findByApplicant(User applicant);
    List<Application> findByJobIn(List<Job> jobs);
}