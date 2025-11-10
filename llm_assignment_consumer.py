#!/usr/bin/env python3
"""
LLM Assignment Consumer
Consumes events from Kafka topic 'course-assignment-llm' and uses LLM
(Cerebras Inference API) to determine if a faculty should be assigned a course.
"""

import json
import os
import requests
import logging
from kafka import KafkaConsumer
from typing import Dict, Any

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Configuration from environment variables
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BROKER', 'localhost:9094')
KAFKA_TOPIC = 'course-assignment-llm'
BACKEND_API_URL = os.getenv('BACKEND_API_URL', 'http://localhost:20001')
CEREBRAS_API_URL = os.getenv('CEREBRAS_API_URL', 'https://api.cerebras.ai/v1/chat/completions')
CEREBRAS_API_KEY = os.getenv('CEREBRAS_API_KEY', '')
FACULTY_EXPERIENCE_API_URL = os.getenv('FACULTY_EXPERIENCE_API_URL', 'https://api.example.com/faculty/{faculty_id}/experience')


def get_faculty_experience(faculty_id: str) -> Dict[str, Any]:
    """
    Fetch faculty experience and publications from external API.
    For now, this is a placeholder that returns mock data.
    """
    try:
        url = FACULTY_EXPERIENCE_API_URL.format(faculty_id=faculty_id)
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            return response.json()
        else:
            logger.warning(f"Failed to fetch experience for faculty {faculty_id}: {response.status_code}")
            return {"experience": [], "publications": []}
    except Exception as e:
        logger.error(f"Error fetching faculty experience: {e}")
        return {"experience": [], "publications": []}


def get_course_details(course_id: str) -> Dict[str, Any]:
    """
    Fetch course details from backend API.
    """
    try:
        url = f"{BACKEND_API_URL}/admin/courses/{course_id}"
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            return response.json()
        else:
            logger.warning(f"Failed to fetch course {course_id}: {response.status_code}")
            return {}
    except Exception as e:
        logger.error(f"Error fetching course details: {e}")
        return {}


def call_llm_for_assignment(faculty_experience: Dict[str, Any], course_details: Dict[str, Any]) -> bool:
    """
    Call Cerebras Inference API to determine if faculty should be assigned the course.
    Returns True if recommended, False otherwise.
    """
    try:
        # Prepare prompt for LLM
        experience_text = ", ".join(faculty_experience.get("experience", []))
        publications_text = ", ".join(faculty_experience.get("publications", []))
        course_name = course_details.get("name", "Unknown Course")
        course_code = course_details.get("code", "Unknown")
        
        prompt = f"""Based on the following information, determine if the faculty member should be assigned to teach this course.

Faculty Experience: {experience_text}
Faculty Publications: {publications_text}
Course: {course_code} - {course_name}

Consider:
1. Relevance of faculty experience to course content
2. Alignment of faculty expertise with course requirements
3. Faculty's publication history related to course topics

Respond with ONLY "YES" or "NO"."""

        # Call Cerebras API
        headers = {
            "Authorization": f"Bearer {CEREBRAS_API_KEY}",
            "Content-Type": "application/json"
        }
        
        payload = {
            "model": "llama-3.1-70b",  # Adjust model as needed
            "messages": [
                {
                    "role": "system",
                    "content": "You are an expert in academic course assignment. Analyze faculty qualifications and course requirements."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            "max_tokens": 10,
            "temperature": 0.1
        }
        
        response = requests.post(CEREBRAS_API_URL, headers=headers, json=payload, timeout=30)
        
        if response.status_code == 200:
            result = response.json()
            answer = result.get("choices", [{}])[0].get("message", {}).get("content", "").strip().upper()
            recommended = answer.startswith("YES")
            logger.info(f"LLM recommendation: {answer} -> {recommended}")
            return recommended
        else:
            logger.error(f"Cerebras API error: {response.status_code} - {response.text}")
            return False
            
    except Exception as e:
        logger.error(f"Error calling LLM: {e}")
        return False


def submit_llm_result(faculty_id: str, course_id: str, recommended: bool) -> bool:
    """
    Submit LLM result to backend API.
    """
    try:
        url = f"{BACKEND_API_URL}/admin/assignments/llm-result"
        payload = {
            "facultyId": faculty_id,
            "courseId": course_id,
            "recommended": recommended
        }
        
        response = requests.post(url, json=payload, timeout=10)
        if response.status_code == 200:
            logger.info(f"Successfully submitted LLM result for faculty {faculty_id} and course {course_id}")
            return True
        else:
            logger.error(f"Failed to submit LLM result: {response.status_code} - {response.text}")
            return False
    except Exception as e:
        logger.error(f"Error submitting LLM result: {e}")
        return False


def process_event(event_data: Dict[str, Any]) -> None:
    """
    Process a single LLM assignment event.
    """
    try:
        faculty_id = event_data.get("facultyId")
        course_id = event_data.get("courseId")
        term_id = event_data.get("termId")
        preference_rank = event_data.get("preferenceRank")
        
        logger.info(f"Processing LLM assignment event: faculty={faculty_id}, course={course_id}, term={term_id}, rank={preference_rank}")
        
        # Get faculty experience
        faculty_experience = get_faculty_experience(str(faculty_id))
        
        # Get course details
        course_details = get_course_details(str(course_id))
        
        # Call LLM
        recommended = call_llm_for_assignment(faculty_experience, course_details)
        
        # Submit result to backend
        submit_llm_result(str(faculty_id), str(course_id), recommended)
        
    except Exception as e:
        logger.error(f"Error processing event: {e}", exc_info=True)


def main():
    """
    Main consumer loop.
    """
    logger.info(f"Starting LLM Assignment Consumer")
    logger.info(f"Kafka broker: {KAFKA_BOOTSTRAP_SERVERS}")
    logger.info(f"Kafka topic: {KAFKA_TOPIC}")
    logger.info(f"Backend API: {BACKEND_API_URL}")
    
    consumer = KafkaConsumer(
        KAFKA_TOPIC,
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_deserializer=lambda m: json.loads(m.decode('utf-8')),
        group_id='llm-assignment-consumer-group',
        auto_offset_reset='earliest',
        enable_auto_commit=True
    )
    
    logger.info("Consumer started, waiting for messages...")
    
    try:
        for message in consumer:
            try:
                event_data = message.value
                process_event(event_data)
            except Exception as e:
                logger.error(f"Error processing message: {e}", exc_info=True)
    except KeyboardInterrupt:
        logger.info("Consumer stopped by user")
    except Exception as e:
        logger.error(f"Consumer error: {e}", exc_info=True)
    finally:
        consumer.close()


if __name__ == "__main__":
    main()

