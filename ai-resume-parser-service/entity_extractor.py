import spacy

nlp = spacy.load("en_core_web_lg")

DEGREE_KEYWORDS = [
    "bachelor", "master", "b.tech", "m.tech",
    "b.e", "m.e", "mba", "phd", "b.sc", "m.sc"
]


def extract_entities(text):
    doc = nlp(text)

    organizations = []
    education = []

    for ent in doc.ents:
        if ent.label_ == "ORG":
            organizations.append(ent.text)

            # Degree detection
            for line in text.split("\n"):
                if any(keyword.lower() in line.lower() for keyword in DEGREE_KEYWORDS):
                    education.append(line.strip())

    return {
        "organizations": organizations,
        "education": list(set(education))
    }
