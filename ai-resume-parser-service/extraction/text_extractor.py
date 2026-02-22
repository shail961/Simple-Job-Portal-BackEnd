import pdfplumber
from docx import Document
from pdfminer.high_level import extract_text
import os
import re


class TextExtractor:

    @staticmethod
    def extract(file_path: str) -> str:
        # Detect file type and extract text accordingly.
        if not os.path.exists(file_path):
            raise FileNotFoundError("File does not exist")

        if file_path.lower().endswith(".pdf"):
            return TextExtractor._extract_pdf(file_path)

        elif file_path.lower().endswith(".docx"):
            return TextExtractor._extract_docx(file_path)

        else:
            raise ValueError("Unsupported file format. Only PDF and DOCX supported.")

    @staticmethod
    def _extract_pdf(file_path: str) -> str:
        return TextExtractor._clean_text(extract_text(file_path))

    @staticmethod
    def _extract_docx(file_path: str) -> str:
        doc = Document(file_path)
        text = "\n".join([para.text for para in doc.paragraphs])
        return TextExtractor._clean_text(text)

    @staticmethod
    def _clean_text(text: str) -> str:
        # Clean weird unicode characters and extra spaces.
        text = re.sub(r'\xa0', ' ', text)  # Replace non-breaking spaces
        text = re.sub(r'[^\x00-\x7F]+', ' ', text)  # Remove non-ASCII chars
        text = re.sub(r'\n+', '\n', text)  # Remove multiple newlines
        text = re.sub(r'[ ]+', ' ', text)  # Remove extra spaces
        return text.strip()
