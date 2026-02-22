import json
import re
from pathlib import Path


class SkillExtractor:

    def __init__(self, skills_file="skills.json"):
        self.skills = self._load_skills(skills_file)

    def _load_skills(self, skills_file):
        path = Path(skills_file)
        with open(path, "r", encoding="utf-8") as f:
            skills = json.load(f)

        # Normalize skills
        return set(skill.lower() for skill in skills)

    def extract(self, text: str):
        text = text.lower()

        extracted_skills = set()

        # Exact + word boundary match
        for skill in self.skills:
            pattern = r'\b' + re.escape(skill) + r'\b'
            if re.search(pattern, text):
                extracted_skills.add(skill)

        return sorted(extracted_skills)
