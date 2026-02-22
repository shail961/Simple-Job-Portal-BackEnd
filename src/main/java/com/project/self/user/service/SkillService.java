package com.project.self.user.service;

import com.project.self.user.entity.Skill;
import com.project.self.user.repository.SkillRepository;
import com.project.self.user.util.SkillNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillService {
    @Autowired
    private SkillRepository skillRepository;

    public Skill getOrCreateSkill(String skillName) {

        String normalized = SkillNormalizer.normalize(skillName);

        return skillRepository.findByNormalizedName(normalized)
                .orElseGet(() -> {
                    Skill skill = new Skill();
                    skill.setName(skillName);
                    skill.setNormalizedName(normalized);
                    return skillRepository.save(skill);
                });
    }
}
