import uuid

from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
import time
import ollama
import chromadb
from chromadb.config import Settings

app = FastAPI()

model = SentenceTransformer("all-MiniLM-L6-v2")
dimension = 384
# Initialize Chroma (persistent)
client = chromadb.Client(
    Settings(persist_directory="./chroma_db")
)

collection = client.get_or_create_collection(
    name="resume_embeddings"
)

resume_chunks = []


class ResumeRequest(BaseModel):
    text: str


from extraction.text_extractor import TextExtractor
from section.section_detector import SectionDetector
from entity.skill_extractor import SkillExtractor
from entity.education_extractor import EducationExtractor
from entity.experience_extractor import ExperienceExtractor

# file_path = "Shaik_Sahil_Ahmed_SDE.pdf"

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
        "skills": skills,
        "resumeText": text
    }


@app.post("/parse-job-description")
async def parse_resume(req: ResumeRequest):
    # skills = skill_extractor.extract(req.text)
    # return {
    #     "skills": skills
    # }
    return skill_extractor.extract_mandatory_optional(req.text, skill_extractor.extract)


def chunk_text(text, chunk_size=300):
    words = text.split()
    chunks = []

    for i in range(0, len(words), chunk_size):
        chunk = " ".join(words[i:i + chunk_size])
        chunks.append(chunk)

    return chunks


@app.post("/rag/explain")
def explain(data: dict):
    start = time.time()
    candidate_id = data["userId"]
    job_text = data["jobDescription"]

    weighted_score = data["weightedScore"]
    mandatory_match = data["mandatoryMatch"]
    optional_match = data["optionalMatch"]

    job_title = data["jobTitle"]
    mandatory_skills = data["mandatorySkills"]
    optional_skills = data["optionalSkills"]
    candidate_skills = data["candidateSkills"]

    # 1️⃣ Embed job description as query
    query_embedding = model.encode([job_text]).tolist()
    print("Embedding time:", time.time() - start)

    start = time.time()

    # 2️⃣ Retrieve relevant resume chunks
    results = collection.query(
        query_embeddings=query_embedding,
        n_results=4,
        where={"candidateId": candidate_id}
    )
    print("Retrieval time:", time.time() - start)

    retrieved_chunks = results["documents"][0] if results["documents"] else []

    context = "\n".join(retrieved_chunks)

    # 3️⃣ Build controlled prompt
    prompt = f"""
You are an AI recruitment assistant.

Job Title: {job_title}

Weighted Match Score: {weighted_score}%
Mandatory Match: {mandatory_match}%
Optional Match: {optional_match}%

Mandatory Skills: {mandatory_skills}
Optional Skills: {optional_skills}
Candidate Skills: {candidate_skills}

Relevant Resume Content:
{context}

Instructions:
- Do NOT calculate scores.
- Do NOT invent skills.
- Only explain each point by point concisely based on provided data.
- Mention strengths.
- Mention missing skills.
- Give a hiring recommendation.

Explain:
    1. Strengths aligned with mandatory skills.
    2. Missing or weak areas.
    3. Overall hiring recommendation.

"""
    start = time.time()

    #  Generate explanation
    response = ollama.chat(
        model="gemma:2b",
        messages=[{"role": "user", "content": prompt}],
        options={
            "num_predict": 120,
            "temperature": 0.2,
            "top_p": 0.8
        }

    )
    print("LLM time:", time.time() - start)

    return {
        "explanation": response["message"]["content"]
    }


@app.post("/embeddings/store")
def store_resume_embedding(data: dict):
    candidate_id = data["userId"]
    resume_text = data["resumeText"]

    chunks = chunk_text(resume_text)

    embeddings = model.encode(chunks)

    vector_id = str(uuid.uuid4())

    ids = [f"{vector_id}_{i}" for i in range(len(chunks))]

    collection.add(
        embeddings=embeddings,
        documents=chunks,
        ids=ids,
        metadatas=[{"candidateId": candidate_id}] * len(chunks)
    )

    return {"vectorId": vector_id}
