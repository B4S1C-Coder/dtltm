package edu.thapar.dtltm.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.thapar.dtltm.model.PreferenceSetCourse;

public interface PreferenceSetCourseRepository extends JpaRepository<PreferenceSetCourse, UUID> {
  List<PreferenceSetCourse> findByPreferenceSet_Id(UUID preferenceSetId);
  void deleteByPreferenceSet_Id(UUID preferenceSetId);
}


