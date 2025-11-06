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

import edu.thapar.dtltm.dto.FacultyUpdateDTO;
import edu.thapar.dtltm.exception.ForbiddenException;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/faculties")
@RequiredArgsConstructor
public class AdminFacultyController {

  private final FacultyService facultyService;

  private void checkAdmin(User user) {
    if (!"ADMIN".equals(user.getRole())) {
      throw new ForbiddenException("This operation is only allowed for admins.");
    }
  }

  @GetMapping
  public ResponseEntity<List<Faculty>> getAllFaculties(@AuthenticationPrincipal User user) {
    checkAdmin(user);
    List<Faculty> faculties = facultyService.getAllFaculties();
    return ResponseEntity.ok(faculties);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Faculty> getFacultyById(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id) {
    checkAdmin(user);
    Faculty faculty = facultyService.getFacultyById(id);
    return ResponseEntity.ok(faculty);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Faculty> updateFaculty(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id,
      @Valid @RequestBody FacultyUpdateDTO dto) {
    checkAdmin(user);
    Faculty faculty = facultyService.updateFaculty(id, dto);
    return ResponseEntity.ok(faculty);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFaculty(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id) {
    checkAdmin(user);
    facultyService.deleteFaculty(id);
    return ResponseEntity.ok().build();
  }
}

