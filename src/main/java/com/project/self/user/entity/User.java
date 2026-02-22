package com.project.self.user.entity;

import com.project.self.user.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role; // RECRUITER or APPLICANT

    @ManyToMany
    @JoinTable(
            name = "candidate_skills",
            joinColumns = @JoinColumn(name = "users_id",               // Column name in the JOIN table
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skills_id",               // Column name in the JOIN table
                    referencedColumnName = "id")
    )
    private Set<Skill> skills = new HashSet<>();
}