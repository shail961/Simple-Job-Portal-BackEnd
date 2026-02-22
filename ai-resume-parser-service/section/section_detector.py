import re
from collections import defaultdict


class SectionDetector:
    SECTION_KEYWORDS = {
        "education": [
            "education", "academic background", "qualifications"
        ],
        "experience": [
            "experience", "work experience", "professional experience"
        ],
        "skills": [
            "skills", "technical skills", "core competencies"
        ],
        "projects": [
            "projects", "academic projects", "personal projects"
        ]
    }

    @staticmethod
    def detect_sections(text: str) -> dict:
        lines = text.split("\n")

        sections = defaultdict(list)
        current_section = "other"

        for line in lines:
            clean_line = line.strip().lower()

            # Check if line is a heading
            detected_section = SectionDetector._match_section(clean_line)

            if detected_section:
                current_section = detected_section
                continue

            sections[current_section].append(line)

        # Convert list to string
        return {
            section: "\n".join(content).strip()
            for section, content in sections.items()
        }

    @staticmethod
    def _match_section(line: str):
        for section, keywords in SectionDetector.SECTION_KEYWORDS.items():
            for keyword in keywords:
                # normalize
                line = re.sub(r'[^a-zA-Z ]', '', line)
                if line == keyword:
                    return section
        return None
