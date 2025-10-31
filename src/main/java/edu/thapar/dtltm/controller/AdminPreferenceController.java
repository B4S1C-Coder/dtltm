package edu.thapar.dtltm.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.CreatePreferenceSetRequest;
import edu.thapar.dtltm.dto.PreferenceSetResponse;
import edu.thapar.dtltm.dto.UpdateCandidateCoursesRequest;
import edu.thapar.dtltm.exception.BadRequestException;
import edu.thapar.dtltm.exception.ConflictException;
import edu.thapar.dtltm.model.PreferenceSet;
import edu.thapar.dtltm.service.PreferenceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/preference-sets")
@RequiredArgsConstructor
public class AdminPreferenceController {

  private final PreferenceService preferenceService;

  @PostMapping
  public ResponseEntity<UUID> create(@RequestBody CreatePreferenceSetRequest req) {
    try {
      PreferenceSet set = preferenceService.createPreferenceSet(req.getFacultyId(), req.getTermId(), req.getCandidateCourseIds());
      return ResponseEntity.ok(set.getId());
    } catch (IllegalStateException e) {
      throw new ConflictException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @PutMapping("/{id}/candidates")
  public ResponseEntity<Void> updateCandidates(@PathVariable("id") UUID id, @RequestBody UpdateCandidateCoursesRequest req) {
    try {
      preferenceService.updateCandidates(id, req.getCandidateCourseIds());
      return ResponseEntity.ok().build();
    } catch (IllegalStateException e) {
      throw new ConflictException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @PostMapping("/{id}/open")
  public ResponseEntity<Void> open(@PathVariable("id") UUID id) {
    preferenceService.openSet(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/close")
  public ResponseEntity<Void> close(@PathVariable("id") UUID id) {
    preferenceService.closeSet(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<PreferenceSetResponse> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(preferenceService.getPreferenceSetResponse(id));
  }
}


