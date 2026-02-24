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

    def extract_mandatory_optional(self, job_text, extract_skills_function):
        result = self.classify_job_skills(job_text, extract_skills_function)

        if not result["mandatorySkills"] and not result["optionalSkills"]:
            # fallback if no sections found
            result = self.classify_by_sentence(job_text, extract_skills_function)

        return result

    def classify_job_skills(self, job_text, extract_skills_function):
        MANDATORY_SECTION_HINTS = [
            "required skills",
            "requirements",
            "must have",
            "mandatory"
        ]

        OPTIONAL_SECTION_HINTS = [
            "good to have",
            "nice to have",
            "preferred",
            "plus"
        ]
        lines = job_text.split("\n")

        mandatory_block = []
        optional_block = []

        current_section = None

        for line in lines:
            lower_line = line.lower().strip()

            # Detect section switches
            if any(hint in lower_line for hint in MANDATORY_SECTION_HINTS):
                current_section = "mandatory"
                continue

            if any(hint in lower_line for hint in OPTIONAL_SECTION_HINTS):
                current_section = "optional"
                continue

            # Collect lines
            if current_section == "mandatory":
                mandatory_block.append(line)

            elif current_section == "optional":
                optional_block.append(line)

        # Extract skills from collected blocks
        mandatory_skills = extract_skills_function("\n".join(mandatory_block))
        optional_skills = extract_skills_function("\n".join(optional_block))

        return {
            "mandatorySkills": list(set(mandatory_skills)),
            "optionalSkills": list(set(optional_skills))
        }

    def classify_by_sentence(self, job_text, extract_skills_function):
        MANDATORY_HINTS = ["must", "required", "strong", "minimum"]
        OPTIONAL_HINTS = ["plus", "preferred", "good to have", "nice to have"]

        sentences = re.split(r'[.!?]\s+', job_text)

        mandatory_skills = []
        optional_skills = []

        for sentence in sentences:
            lower_sentence = sentence.lower()

            skills = extract_skills_function(sentence)

            if any(hint in lower_sentence for hint in MANDATORY_HINTS):
                mandatory_skills.extend(skills)

            elif any(hint in lower_sentence for hint in OPTIONAL_HINTS):
                optional_skills.extend(skills)

            else:
                # If neutral sentence → treat as mandatory (business rule)
                mandatory_skills.extend(skills)

        return {
            "mandatorySkills": list(set(mandatory_skills)),
            "optionalSkills": list(set(optional_skills))
        }
