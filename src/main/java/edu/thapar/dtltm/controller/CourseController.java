package edu.thapar.dtltm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.CourseCreationDTO;
import edu.thapar.dtltm.exception.ForbiddenException;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;

  @PostMapping("create")
  public ResponseEntity<Void> create(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody CourseCreationDTO dto
  ) {
    if (!"ADMIN".equals(user.getRole())) {
      throw new ForbiddenException("Course creation is only allowed for admins.");
    }

    courseService.createCourse(dto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
