package edu.thapar.dtltm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.thapar.dtltm.model.PreferenceSet;
import edu.thapar.dtltm.model.PreferenceSet.Status;

public interface PreferenceSetRepository extends JpaRepository<PreferenceSet, UUID> {
  Optional<PreferenceSet> findByFaculty_IdAndTerm_Id(UUID facultyId, UUID termId);
  List<PreferenceSet> findByFaculty_Id(UUID facultyId);
  List<PreferenceSet> findByFaculty_IdAndStatus(UUID facultyId, Status status);
  List<PreferenceSet> findByTerm_Id(UUID termId);
}


