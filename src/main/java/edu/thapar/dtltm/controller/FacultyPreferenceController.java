package edu.thapar.dtltm.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.PreferenceSetResponse;
import edu.thapar.dtltm.dto.SubmitPreferencesRequest;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.exception.ResourceNotFoundException;
import edu.thapar.dtltm.repository.FacultyRepository;
import edu.thapar.dtltm.service.PreferenceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/faculty/preference-sets")
@RequiredArgsConstructor
public class FacultyPreferenceController {

  private final PreferenceService preferenceService;
  private final FacultyRepository facultyRepository;

  @PostMapping("/{id}/submit")
  public ResponseEntity<Void> submit(
      @PathVariable("id") UUID setId,
      @AuthenticationPrincipal User user,
      @RequestBody SubmitPreferencesRequest req) {
    Faculty faculty = facultyRepository.findByUser_Id(user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found for current user"));
    preferenceService.submitPreferences(setId, faculty.getId(), req.getRankedCourseIds());
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<List<PreferenceSetResponse>> listOpen(@AuthenticationPrincipal User user) {
    Faculty faculty = facultyRepository.findByUser_Id(user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found for current user"));
    return ResponseEntity.ok(preferenceService.getOpenSetsForFaculty(faculty.getId()));
  }
}


