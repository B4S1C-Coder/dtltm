package edu.thapar.dtltm.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.thapar.dtltm.model.FacultyCoursePreference;

public interface FacultyCoursePreferenceRepository extends JpaRepository<FacultyCoursePreference, UUID> {
  List<FacultyCoursePreference> findByPreferenceSet_IdOrderByRankAsc(UUID preferenceSetId);
  void deleteByPreferenceSet_Id(UUID preferenceSetId);
}


