package edu.thapar.dtltm.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.thapar.dtltm.dto.PreferenceSetResponse;
import edu.thapar.dtltm.model.AcademicTerm;
import edu.thapar.dtltm.model.Course;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.model.FacultyCoursePreference;
import edu.thapar.dtltm.model.PreferenceSet;
import edu.thapar.dtltm.model.PreferenceSet.Status;
import edu.thapar.dtltm.model.PreferenceSetCourse;
import edu.thapar.dtltm.repository.AcademicTermRepository;
import edu.thapar.dtltm.repository.CourseRepository;
import edu.thapar.dtltm.repository.FacultyCoursePreferenceRepository;
import edu.thapar.dtltm.repository.FacultyRepository;
import edu.thapar.dtltm.repository.PreferenceSetCourseRepository;
import edu.thapar.dtltm.repository.PreferenceSetRepository;

@Service
public class PreferenceService {

  private final FacultyRepository facultyRepository;
  private final AcademicTermRepository academicTermRepository;
  private final CourseRepository courseRepository;
  private final PreferenceSetRepository preferenceSetRepository;
  private final PreferenceSetCourseRepository preferenceSetCourseRepository;
  private final FacultyCoursePreferenceRepository facultyCoursePreferenceRepository;

  public PreferenceService(
      FacultyRepository facultyRepository,
      AcademicTermRepository academicTermRepository,
      CourseRepository courseRepository,
      PreferenceSetRepository preferenceSetRepository,
      PreferenceSetCourseRepository preferenceSetCourseRepository,
      FacultyCoursePreferenceRepository facultyCoursePreferenceRepository) {
    this.facultyRepository = facultyRepository;
    this.academicTermRepository = academicTermRepository;
    this.courseRepository = courseRepository;
    this.preferenceSetRepository = preferenceSetRepository;
    this.preferenceSetCourseRepository = preferenceSetCourseRepository;
    this.facultyCoursePreferenceRepository = facultyCoursePreferenceRepository;
  }

  @Transactional
  public PreferenceSet createPreferenceSet(UUID facultyId, UUID termId, List<UUID> candidateCourseIds) {
    Faculty faculty = facultyRepository.findById(facultyId)
        .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
    AcademicTerm term = academicTermRepository.findById(termId)
        .orElseThrow(() -> new IllegalArgumentException("Term not found"));

    Optional<PreferenceSet> existing = preferenceSetRepository.findByFaculty_IdAndTerm_Id(facultyId, termId);
    if (existing.isPresent()) {
      throw new IllegalStateException("Preference set already exists for faculty and term");
    }

    PreferenceSet set = PreferenceSet.builder()
        .faculty(faculty)
        .term(term)
        .status(Status.DRAFT)
        .build();
    set = preferenceSetRepository.save(set);

    List<Course> courses = courseRepository.findAllById(candidateCourseIds);
    if (courses.size() != new HashSet<>(candidateCourseIds).size()) {
      throw new IllegalArgumentException("Some candidate courses do not exist");
    }
    List<PreferenceSetCourse> links = new ArrayList<>();
    for (Course c : courses) {
      links.add(PreferenceSetCourse.builder().preferenceSet(set).course(c).build());
    }
    preferenceSetCourseRepository.saveAll(links);
    return set;
  }

  @Transactional
  public PreferenceSet updateCandidates(UUID setId, List<UUID> candidateCourseIds) {
    PreferenceSet set = preferenceSetRepository.findById(setId)
        .orElseThrow(() -> new IllegalArgumentException("Preference set not found"));
    if (set.getStatus() == Status.CLOSED) {
      throw new IllegalStateException("Cannot update candidates for a CLOSED set");
    }

    // Verify all course IDs valid
    List<Course> courses = courseRepository.findAllById(candidateCourseIds);
    if (courses.size() != new HashSet<>(candidateCourseIds).size()) {
      throw new IllegalArgumentException("Some candidate courses do not exist");
    }

    // Check for conflict with existing preferences
    Set<UUID> courseIdSet = courses.stream().map(Course::getId).collect(Collectors.toSet());
    List<FacultyCoursePreference> existingPrefs = facultyCoursePreferenceRepository
        .findByPreferenceSet_IdOrderByRankAsc(setId);
    boolean conflict = existingPrefs.stream().anyMatch(p -> !courseIdSet.contains(p.getCourse().getId()));
    if (conflict) {
      throw new IllegalStateException("Cannot remove courses that are already in submitted preferences");
    }

    preferenceSetCourseRepository.deleteByPreferenceSet_Id(setId);
    List<PreferenceSetCourse> links = new ArrayList<>();
    for (Course c : courses) {
      links.add(PreferenceSetCourse.builder().preferenceSet(set).course(c).build());
    }
    preferenceSetCourseRepository.saveAll(links);
    return set;
  }

  @Transactional
  public PreferenceSet openSet(UUID setId) {
    PreferenceSet set = preferenceSetRepository.findById(setId)
        .orElseThrow(() -> new IllegalArgumentException("Preference set not found"));
    set.setStatus(Status.OPEN);
    return preferenceSetRepository.save(set);
  }

  @Transactional
  public PreferenceSet closeSet(UUID setId) {
    PreferenceSet set = preferenceSetRepository.findById(setId)
        .orElseThrow(() -> new IllegalArgumentException("Preference set not found"));
    set.setStatus(Status.CLOSED);
    return preferenceSetRepository.save(set);
  }

  @Transactional
  public void submitPreferences(UUID setId, UUID facultyId, List<UUID> rankedCourseIds) {
    PreferenceSet set = preferenceSetRepository.findById(setId)
        .orElseThrow(() -> new IllegalArgumentException("Preference set not found"));
    if (!set.getFaculty().getId().equals(facultyId)) {
      throw new SecurityException("Faculty cannot submit for another faculty's preference set");
    }
    if (set.getStatus() != Status.OPEN) {
      throw new IllegalStateException("Preference set is not OPEN");
    }
    // No duplicates
    if (new HashSet<>(rankedCourseIds).size() != rankedCourseIds.size()) {
      throw new IllegalArgumentException("Duplicate courses in ranking are not allowed");
    }
    // All must be subset of candidates
    Set<UUID> candidateIds = preferenceSetCourseRepository.findByPreferenceSet_Id(setId).stream()
        .map(link -> link.getCourse().getId())
        .collect(Collectors.toSet());
    if (!candidateIds.containsAll(rankedCourseIds)) {
      throw new IllegalArgumentException("All ranked courses must be in candidate set");
    }
    // Strict contiguous ranks 1..k enforced by our creation order and DB unique constraint

    facultyCoursePreferenceRepository.deleteByPreferenceSet_Id(setId);
    List<FacultyCoursePreference> toSave = new ArrayList<>();
    int rank = 1;
    for (UUID courseId : rankedCourseIds) {
      Course course = courseRepository.findById(courseId)
          .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
      toSave.add(FacultyCoursePreference.builder()
          .preferenceSet(set)
          .course(course)
          .rank(rank++)
          .build());
    }
    facultyCoursePreferenceRepository.saveAll(toSave);
  }

  @Transactional(readOnly = true)
  public PreferenceSetResponse getPreferencesByFacultyAndTerm(UUID facultyId, UUID termId) {
    PreferenceSet set = preferenceSetRepository.findByFaculty_IdAndTerm_Id(facultyId, termId)
        .orElseThrow(() -> new IllegalArgumentException("Preference set not found"));

    List<UUID> candidateIds = preferenceSetCourseRepository.findByPreferenceSet_Id(set.getId()).stream()
        .map(link -> link.getCourse().getId()).toList();
    List<PreferenceSetResponse.Preference> prefs = facultyCoursePreferenceRepository
        .findByPreferenceSet_IdOrderByRankAsc(set.getId()).stream()
        .map(p -> PreferenceSetResponse.Preference.builder()
            .courseId(p.getCourse().getId())
            .rank(p.getRank())
            .build())
        .toList();

    return PreferenceSetResponse.builder()
        .id(set.getId())
        .facultyId(set.getFaculty().getId())
        .term(PreferenceSetResponse.Term.builder()
            .year(set.getTerm().getYear())
            .season(set.getTerm().getSeason())
            .build())
        .candidateCourseIds(candidateIds)
        .preferences(prefs)
        .status(set.getStatus())
        .build();
  }

  @Transactional(readOnly = true)
  public List<PreferenceSetResponse> getOpenSetsForFaculty(UUID facultyId) {
    List<PreferenceSet> sets = preferenceSetRepository.findByFaculty_IdAndStatus(facultyId, Status.OPEN);
    return sets.stream().map(s -> getPreferencesByFacultyAndTerm(s.getFaculty().getId(), s.getTerm().getId()))
        .toList();
  }

  @Transactional(readOnly = true)
  public PreferenceSetResponse getPreferenceSetResponse(UUID setId) {
    PreferenceSet set = preferenceSetRepository.findById(setId)
        .orElseThrow(() -> new IllegalArgumentException("Preference set not found"));
    return getPreferencesByFacultyAndTerm(set.getFaculty().getId(), set.getTerm().getId());
  }
}


