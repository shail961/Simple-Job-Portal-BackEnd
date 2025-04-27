package com.project.self.user.repository;

import com.project.self.user.entity.Job;
import com.project.self.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByPostedBy(User user);
}