from sentence_transformers import SentenceTransformer, util
import json

model = SentenceTransformer('all-MiniLM-L6-v2')

skills = json.load(open("skills.json"))
skill_embeddings = model.encode(skills, convert_to_tensor=True)

def extract_skills(text):
    sentences = text.split("\n")
    detected = set()

    for sentence in sentences:
        sent_embedding = model.encode(sentence, convert_to_tensor=True)
        similarity = util.cos_sim(sent_embedding, skill_embeddings)

        for idx, score in enumerate(similarity[0]):
            if score > 0.6:
                detected.add(skills[idx])

    return list(detected)
