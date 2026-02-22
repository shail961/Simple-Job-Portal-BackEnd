package com.project.self.user.util;

public class SkillNormalizer {

    public static String normalize(String skill) {
        return skill.toLowerCase()
                .replaceAll("[^a-z0-9+.#]", "")
                .trim();
    }
}
