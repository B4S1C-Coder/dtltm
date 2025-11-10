package edu.thapar.dtltm.kafka;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentKafkaProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private static final String COURSE_ASSIGNMENT_LLM_TOPIC = "course-assignment-llm";

  public void sendLLMAssignmentEvent(UUID facultyId, UUID courseId, UUID termId, Integer preferenceRank) {
    try {
      LLMAssignmentEvent event = new LLMAssignmentEvent(facultyId, courseId, termId, preferenceRank);
      String eventJson = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(COURSE_ASSIGNMENT_LLM_TOPIC, facultyId.toString(), eventJson);
      log.info("Published LLM assignment event for faculty {} and course {}", facultyId, courseId);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize LLM assignment event", e);
      throw new RuntimeException("Failed to publish LLM assignment event", e);
    }
  }

  public static class LLMAssignmentEvent {
    private UUID facultyId;
    private UUID courseId;
    private UUID termId;
    private Integer preferenceRank;

    public LLMAssignmentEvent() {
    }

    public LLMAssignmentEvent(UUID facultyId, UUID courseId, UUID termId, Integer preferenceRank) {
      this.facultyId = facultyId;
      this.courseId = courseId;
      this.termId = termId;
      this.preferenceRank = preferenceRank;
    }

    public UUID getFacultyId() {
      return facultyId;
    }

    public void setFacultyId(UUID facultyId) {
      this.facultyId = facultyId;
    }

    public UUID getCourseId() {
      return courseId;
    }

    public void setCourseId(UUID courseId) {
      this.courseId = courseId;
    }

    public UUID getTermId() {
      return termId;
    }

    public void setTermId(UUID termId) {
      this.termId = termId;
    }

    public Integer getPreferenceRank() {
      return preferenceRank;
    }

    public void setPreferenceRank(Integer preferenceRank) {
      this.preferenceRank = preferenceRank;
    }
  }
}

