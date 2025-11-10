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

import edu.thapar.dtltm.dto.FacultyResponseDTO;
import edu.thapar.dtltm.dto.FacultyUpdateDTO;
import edu.thapar.dtltm.exception.ForbiddenException;
import edu.thapar.dtltm.mapper.FacultyMapper;
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
  public ResponseEntity<List<FacultyResponseDTO>> getAllFaculties(@AuthenticationPrincipal User user) {
    checkAdmin(user);
    List<FacultyResponseDTO> faculties = facultyService
        .getAllFaculties().stream().map(
          f -> FacultyMapper.toDTO(f)
        ).toList();
    return ResponseEntity.ok(faculties);
  }

  @GetMapping("/{id}")
  public ResponseEntity<FacultyResponseDTO> getFacultyById(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id) {
    checkAdmin(user);
    Faculty faculty = facultyService.getFacultyById(id);
    return ResponseEntity.ok(FacultyMapper.toDTO(faculty));
  }

  @PutMapping("/{id}")
  public ResponseEntity<FacultyResponseDTO> updateFaculty(
      @AuthenticationPrincipal User user,
      @PathVariable UUID id,
      @Valid @RequestBody FacultyUpdateDTO dto) {
    checkAdmin(user);
    Faculty faculty = facultyService.updateFaculty(id, dto);
    return ResponseEntity.ok(FacultyMapper.toDTO(faculty));
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

