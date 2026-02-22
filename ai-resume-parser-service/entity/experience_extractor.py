import re
import spacy

nlp = spacy.load("en_core_web_lg")

# PATTERNS
DATE_PATTERN = r"(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s?\d{4}\s?(-|–)?\s?(Present|(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s?\d{4})"

ROLE_KEYWORDS = [
    "engineer", "developer", "intern",
    "analyst", "manager", "architect",
    "consultant", "lead", "associate",
    "specialist", "officer"
]


# CLASSIFIERS
def is_date(line):
    return re.search(DATE_PATTERN, line, re.IGNORECASE)


def is_location(line):
    # Simple heuristic: contains comma & short length
    return "," in line and len(line.split()) <= 6


def is_role(line):
    return any(keyword in line.lower() for keyword in ROLE_KEYWORDS)


def is_company(line):
    doc = nlp(line)
    for ent in doc.ents:
        if ent.label_ == "ORG":
            # Avoid role misclassification
            if not is_role(line):
                return True
    return False


# EXPERIENCE EXTRACTOR
class ExperienceExtractor:

    @staticmethod
    def extract(experience_text: str):

        lines = [l.strip("• ").strip() for l in experience_text.split("\n") if l.strip()]

        entries = []
        i = 0

        while i < len(lines):

            if is_date(lines[i]):

                date_range = lines[i]

                # Look at previous few lines for header info
                header_window = lines[max(0, i - 4):i]

                role = None
                company = None
                location = None

                for line in header_window:

                    if is_role(line):
                        role = line
                    elif is_location(line):
                        location = line
                    elif is_company(line):
                        company = line

                # Extract description lines until next date
                description = []
                j = i + 1
                while j < len(lines) and not is_date(lines[j]):
                    description.append(lines[j])
                    j += 1

                entries.append({
                    "company": company,
                    "role": role,
                    "location": location,
                    "date_range": date_range,
                    "description": description
                })

                i = j  # jump to next block
            else:
                i += 1

        return entries
