package edu.thapar.dtltm.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.AssignmentRequest;
import edu.thapar.dtltm.dto.AssignmentResponse;
import edu.thapar.dtltm.dto.LLMResultRequest;
import edu.thapar.dtltm.dto.UpdateAssignmentRequest;
import edu.thapar.dtltm.exception.ForbiddenException;
import edu.thapar.dtltm.model.Course;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/assignments")
@RequiredArgsConstructor
public class AdminAssignmentController {

  private final AssignmentService assignmentService;

  private void checkAdmin(User user) {
    if (!"ADMIN".equals(user.getRole())) {
      throw new ForbiddenException("This operation is only allowed for admins.");
    }
  }

  @PostMapping("/run")
  public ResponseEntity<List<AssignmentResponse>> runAssignment(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody AssignmentRequest request) {
    checkAdmin(user);
    List<AssignmentResponse> responses = assignmentService.assignCoursesAutomatic(request.getTermId());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/term/{termId}")
  public ResponseEntity<List<AssignmentResponse>> getAssignmentsByTerm(
      @AuthenticationPrincipal User user,
      @PathVariable UUID termId) {
    checkAdmin(user);
    List<AssignmentResponse> responses = assignmentService.assignCoursesAutomatic(termId);
    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{facultyId}/courses")
  public ResponseEntity<Void> updateFacultyAssignments(
      @AuthenticationPrincipal User user,
      @PathVariable UUID facultyId,
      @Valid @RequestBody UpdateAssignmentRequest request) {
    checkAdmin(user);
    assignmentService.updateFacultyAssignments(facultyId, request.getCourseIds());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{facultyId}/courses/{courseId}")
  public ResponseEntity<Void> removeCourseAssignment(
      @AuthenticationPrincipal User user,
      @PathVariable UUID facultyId,
      @PathVariable UUID courseId) {
    checkAdmin(user);
    assignmentService.removeCourseAssignment(facultyId, courseId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/llm-result")
  public ResponseEntity<Void> processLLMResult(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody LLMResultRequest request) {
    checkAdmin(user);
    assignmentService.processLLMResult(
        request.getFacultyId(),
        request.getCourseId(),
        request.getRecommended());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/unassigned/{termId}")
  public ResponseEntity<List<Course>> getUnassignedCourses(
      @AuthenticationPrincipal User user,
      @PathVariable UUID termId) {
    checkAdmin(user);
    List<Course> courses = assignmentService.getUnassignedCourses(termId);
    return ResponseEntity.ok(courses);
  }

  @GetMapping("/faculty/{facultyId}/term/{termId}")
  public ResponseEntity<List<Course>> getFacultyAssignments(
      @AuthenticationPrincipal User user,
      @PathVariable UUID facultyId,
      @PathVariable UUID termId) {
    checkAdmin(user);
    List<Course> courses = assignmentService.getFacultyAssignments(facultyId, termId);
    return ResponseEntity.ok(courses);
  }
}

