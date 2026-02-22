from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from skill_extractor import extract_skills
from entity_extractor import extract_entities

app = FastAPI()


class ResumeRequest(BaseModel):
    text: str


from extraction.text_extractor import TextExtractor
from section.section_detector import SectionDetector
from entity.skill_extractor import SkillExtractor
from entity.education_extractor import EducationExtractor
from entity.experience_extractor import ExperienceExtractor

file_path = "Shaik_Sahil_Ahmed_SDE.pdf"

# text = TextExtractor.extract(file_path)
# print(text)
# sections = SectionDetector.detect_sections(text)
# skill_extractor = SkillExtractor()

# skills_section = sections.get("skills", "")
# skills = skill_extractor.extract(skills_section)
# education_section = sections.get("education", "")
# education_data = EducationExtractor.extract(education_section)
# experience_section = sections.get("experience", "")
# experience_data = ExperienceExtractor.extract(experience_section)
# print("skills:", skills)
skill_extractor = SkillExtractor()


@app.post("/parse-resume")
async def parse_resume(file: UploadFile):
    contents = await file.read()

    with open("temp.pdf", "wb") as f:
        f.write(contents)

    text = TextExtractor.extract("temp.pdf")
    sections = SectionDetector.detect_sections(text)
    skills_section = sections.get("skills", "")
    skills = skill_extractor.extract(skills_section)

    return {
        "skills": skills
    }


@app.post("/parse-job-description")
async def parse_resume(req: ResumeRequest):
    skills = skill_extractor.extract(req.text)
    return {
        "skills": skills
    }
