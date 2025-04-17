#!/usr/bin/env python3
"""
NER Service for PrivSense PII Detection
This Flask application provides Named Entity Recognition services
specifically tuned for identifying Personally Identifiable Information.
"""

import os
import json
import logging
from typing import List, Dict, Any, Optional, Tuple

import spacy
from flask import Flask, request, jsonify
from flask.logging import create_logger

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)

# Initialize Flask app
app = Flask(__name__)
LOG = create_logger(app)

# Load the spaCy model - using the English model by default
# Options could be: en_core_web_sm, en_core_web_md, en_core_web_lg
try:
    NLP = spacy.load("en_core_web_md")
    LOG.info("Loaded spaCy model: en_core_web_md")
except OSError:
    # If the model isn't installed, download it and try again
    LOG.warning("spaCy model not found, attempting to download...")
    import subprocess
    subprocess.run([
        "python", "-m", "spacy", "download", "en_core_web_md"
    ], check=True)
    NLP = spacy.load("en_core_web_md")
    LOG.info("Downloaded and loaded spaCy model: en_core_web_md")

# Define PII entity types and their mapping to spaCy entities
PII_ENTITY_MAPPING = {
    "PERSON": "NAME",
    "ORG": "OTHER",  # Organizations might contain PII in some contexts
    "GPE": "ADDRESS",  # Geo-political entities (cities, countries, etc.)
    "LOC": "ADDRESS",  # Non-GPE locations
    "DATE": "DATE_OF_BIRTH",  # Dates might be birthdays
    "CARDINAL": "OTHER",  # Numbers that might be part of IDs or accounts
}

# Custom regex patterns for common PII types
CUSTOM_PATTERNS = [
    {"label": "EMAIL", "pattern": r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b"},
    {"label": "PHONE", "pattern": r"\b(\+\d{1,2}\s?)?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}\b"},
    {"label": "SSN", "pattern": r"\b\d{3}[-]?\d{2}[-]?\d{4}\b"},
    {"label": "CREDIT_CARD", "pattern": r"\b(?:\d{4}[- ]?){3}\d{4}\b"},
    {"label": "IP_ADDRESS", "pattern": r"\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b"}
]

# Add custom patterns to the NLP pipeline
ruler = NLP.add_pipe("entity_ruler", before="ner")
patterns = []
for item in CUSTOM_PATTERNS:
    patterns.append({"label": item["label"], "pattern": [{"TEXT": {"REGEX": item["pattern"]}}]})
ruler.add_patterns(patterns)


def sanitize_for_logging(text: str) -> str:
    """Sanitizes potentially sensitive text for logging purposes."""
    if not text:
        return ""
    if len(text) <= 4:
        return "****"
    # Show only first and last character, mask the rest
    return text[0] + '*' * (min(len(text) - 2, 8)) + text[-1]


def analyze_text(texts: List[str]) -> Tuple[List[Dict[str, Any]], float]:
    """
    Analyze a list of text samples for named entities that could be PII.
    
    Args:
        texts: List of text samples to analyze
        
    Returns:
        Tuple containing a list of detected entities and overall confidence score
    """
    if not texts:
        return [], 0.0
    
    entities = []
    total_confidence = 0.0
    
    for text in texts:
        if not text or not text.strip():
            continue
            
        doc = NLP(text)
        
        # Process each entity found by spaCy
        for ent in doc.ents:
            # Map spaCy entity types to PII types
            pii_type = PII_ENTITY_MAPPING.get(ent.label_, ent.label_)
            
            # Calculate confidence based on entity recognition
            # This is a simplified approach - in production, you'd use a more sophisticated model
            confidence = 0.85  # Base confidence for detected entities
            
            # Boost confidence for certain entity types
            if ent.label_ in ["PERSON", "EMAIL", "PHONE", "SSN", "CREDIT_CARD"]:
                confidence = 0.95
            
            entity_data = {
                "text": ent.text,
                "type": pii_type,
                "confidence": confidence,
                "startIndex": ent.start_char,
                "endIndex": ent.end_char
            }
            
            entities.append(entity_data)
            total_confidence += confidence
    
    # Calculate overall confidence
    overall_confidence = total_confidence / max(len(entities), 1)
    
    return entities, overall_confidence


@app.route("/health", methods=["GET"])
def health_check():
    """Health check endpoint for monitoring."""
    return jsonify({"status": "healthy", "model": "en_core_web_md"})


@app.route("/ner/analyze", methods=["POST"])
def analyze():
    """
    Endpoint for analyzing text samples for PII using NER.
    
    Expected JSON payload:
    {
        "texts": ["sample text 1", "sample text 2", ...]
    }
    
    Returns JSON with detected entities and confidence scores.
    """
    try:
        data = request.json
        if not data or "texts" not in data:
            return jsonify({"error": "Invalid request. 'texts' field is required"}), 400
        
        texts = data["texts"]
        if not isinstance(texts, list):
            return jsonify({"error": "Invalid request. 'texts' must be a list"}), 400
        
        # Log some info about the request (sanitized)
        sanitized_samples = [sanitize_for_logging(text) for text in texts[:2]]
        LOG.info(f"Processing {len(texts)} text samples. Examples: {sanitized_samples}...")
        
        # Analyze the text samples
        entities, overall_confidence = analyze_text(texts)
        
        # Return the results
        return jsonify({
            "entities": entities,
            "overallConfidence": overall_confidence,
            "count": len(entities)
        })
    
    except Exception as e:
        LOG.error(f"Error processing request: {str(e)}", exc_info=True)
        return jsonify({
            "error": "Internal server error",
            "message": str(e)
        }), 500


if __name__ == "__main__":
    # Get port from environment variable or use 5000 as default
    port = int(os.environ.get("PORT", 5000))
    
    # Run the Flask application
    app.run(host="0.0.0.0", port=port, debug=False)
