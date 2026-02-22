import spacy
import re

nlp = spacy.load("en_core_web_lg")

DEGREE_KEYWORDS = [
    "bachelor", "master", "b.tech", "m.tech",
    "b.e", "m.e", "b.sc", "m.sc",
    "mba", "phd", "doctorate"
]

INSTITUTION_HINTS = [
    "university", "institute", "college",
    "school", "technology"
]


class EducationExtractor:

    @staticmethod
    def extract(education_text: str):

        lines = [line.strip() for line in education_text.split("\n") if line.strip()]

        degree = None
        institution = None
        duration = None

        for line in lines:

            lower_line = line.lower()

            # Extract duration
            year_match = re.findall(r'(19|20)\d{2}', line)
            if len(year_match) >= 1:
                duration = line

            # Extract degree
            if any(keyword in lower_line for keyword in DEGREE_KEYWORDS):
                degree = line

            # Extract institution
            if any(hint in lower_line for hint in INSTITUTION_HINTS):
                institution = line

        if degree or institution:
            return [{
                "degree": degree,
                "institution": institution,
                "duration": duration
            }]

        return []
