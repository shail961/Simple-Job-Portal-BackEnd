package com.project.self.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private Double salary;

    @ManyToOne
    private User postedBy;

    @Transient
    private boolean applied = false;

    @Transient
    private double score = 0.0;

    @ManyToMany
    @JoinTable(
            name = "job_mandatory_skills",
            joinColumns = @JoinColumn(name = "jobs_id"),
            inverseJoinColumns = @JoinColumn(name = "skills_id")
    )
    private Set<Skill> mandatorySkills = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "job_optional_skills",
            joinColumns = @JoinColumn(name = "jobs_id"),
            inverseJoinColumns = @JoinColumn(name = "skills_id")
    )
    private Set<Skill> optionalSkills = new HashSet<>();

}