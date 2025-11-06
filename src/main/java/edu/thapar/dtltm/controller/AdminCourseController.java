package edu.thapar.dtltm.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.CourseUpdateDTO;
import edu.thapar.dtltm.exception.ForbiddenException;
import edu.thapar.dtltm.model.Course;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

  private final CourseService courseService;

  private void checkAdmin(User user) {
    if (!"ADMIN".equals(user.getRole())) {
      throw new ForbiddenException("This operation is only allowed for admins.");
    }
  }

  @GetMapping
  public ResponseEntity<List<Course>> getAllCourses(@AuthenticationPrincipal User user) {
    checkAdmin(user);
    List<Course> courses = courseService.getAllCourses();
    return ResponseEntity.ok(courses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Course> getCourseById(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id) {
    checkAdmin(user);
    Course course = courseService.getCourseById(id);
    return ResponseEntity.ok(course);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Course> updateCourse(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id,
      @Valid @RequestBody CourseUpdateDTO dto) {
    checkAdmin(user);
    Course course = courseService.updateCourse(id, dto);
    return ResponseEntity.ok(course);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCourse(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id) {
    checkAdmin(user);
    courseService.deleteCourse(id);
    return ResponseEntity.ok().build();
  }
}

