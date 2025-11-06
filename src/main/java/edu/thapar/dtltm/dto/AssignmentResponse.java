package edu.thapar.dtltm.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponse {
  private UUID facultyId;
  private String facultyName;
  private List<AssignedCourse> assignedCourses;
  private List<UUID> unassignedCourseIds;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AssignedCourse {
    private UUID courseId;
    private String courseCode;
    private String courseName;
    private Integer hoursRequiredPerWeek;
  }
}

