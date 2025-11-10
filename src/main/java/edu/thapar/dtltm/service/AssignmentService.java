package edu.thapar.dtltm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.thapar.dtltm.dto.AssignmentResponse;
import edu.thapar.dtltm.exception.BadRequestException;
import edu.thapar.dtltm.kafka.AssignmentKafkaProducer;
import edu.thapar.dtltm.model.AcademicTerm;
import edu.thapar.dtltm.model.Course;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.model.FacultyCoursePreference;
import edu.thapar.dtltm.model.PreferenceSet;
import edu.thapar.dtltm.model.PreferenceSet.Status;
import edu.thapar.dtltm.repository.AcademicTermRepository;
import edu.thapar.dtltm.repository.CourseRepository;
import edu.thapar.dtltm.repository.FacultyCoursePreferenceRepository;
import edu.thapar.dtltm.repository.FacultyRepository;
import edu.thapar.dtltm.repository.PreferenceSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {
  private final AcademicTermRepository academicTermRepository;
  private final PreferenceSetRepository preferenceSetRepository;
  private final FacultyCoursePreferenceRepository facultyCoursePreferenceRepository;
  private final CourseRepository courseRepository;
  private final FacultyRepository facultyRepository;
  private final AssignmentKafkaProducer kafkaProducer;

  @Transactional
  public List<AssignmentResponse> assignCoursesAutomatic(UUID termId) {
    AcademicTerm term = academicTermRepository.findById(termId)
        .orElseThrow(() -> new BadRequestException("Term not found"));

    // Get all preference sets for this term that are CLOSED (preferences submitted)
    List<PreferenceSet> preferenceSets = preferenceSetRepository.findByTerm_Id(termId)
        .stream()
        .filter(set -> set.getStatus() == Status.CLOSED)
        .toList();

    if (preferenceSets.isEmpty()) {
      throw new BadRequestException("No closed preference sets found for term");
    }

    // Get all courses that need assignment and map to all interested faculties
    Set<UUID> allCourseIds = new HashSet<>();
    Map<UUID, List<FacultyCandidate>> courseToFacultiesMap = new HashMap<>(); // courseId -> list of interested faculties
    
    for (PreferenceSet set : preferenceSets) {
      List<FacultyCoursePreference> preferences = facultyCoursePreferenceRepository
          .findByPreferenceSet_IdOrderByRankAsc(set.getId());
      for (FacultyCoursePreference pref : preferences) {
        UUID courseId = pref.getCourse().getId();
        allCourseIds.add(courseId);
        
        FacultyCandidate candidate = new FacultyCandidate(
            set.getFaculty(),
            set.getId(),
            pref.getRank()
        );
        courseToFacultiesMap.computeIfAbsent(courseId, k -> new ArrayList<>()).add(candidate);
      }
    }

    // Track assignments
    Map<UUID, List<Course>> facultyAssignments = new HashMap<>(); // facultyId -> assigned courses
    Map<UUID, Integer> facultyHoursUsed = new HashMap<>(); // facultyId -> hours used
    Set<UUID> assignedCourseIds = new HashSet<>();
    List<Course> unassignedCourses = new ArrayList<>();

    // Get all courses
    List<Course> coursesToAssign = courseRepository.findAllById(allCourseIds);

    // Sort courses by priority (prefer courses with higher preference ranks)
    coursesToAssign.sort((c1, c2) -> {
      List<FacultyCandidate> candidates1 = courseToFacultiesMap.getOrDefault(c1.getId(), new ArrayList<>());
      List<FacultyCandidate> candidates2 = courseToFacultiesMap.getOrDefault(c2.getId(), new ArrayList<>());
      
      if (candidates1.isEmpty() || candidates2.isEmpty()) return 0;
      
      // Use best (lowest) preference rank
      Integer bestRank1 = candidates1.stream().mapToInt(c -> c.preferenceRank).min().orElse(Integer.MAX_VALUE);
      Integer bestRank2 = candidates2.stream().mapToInt(c -> c.preferenceRank).min().orElse(Integer.MAX_VALUE);
      return bestRank1.compareTo(bestRank2);
    });

    // Assign courses
    for (Course course : coursesToAssign) {
      if (assignedCourseIds.contains(course.getId())) {
        continue; // Already assigned
      }

      // Find best faculty for this course among all interested faculties
      List<FacultyCandidate> candidates = courseToFacultiesMap.getOrDefault(course.getId(), new ArrayList<>());
      FacultyCandidate bestCandidate = findBestFacultyCandidate(
          course,
          candidates,
          facultyAssignments,
          facultyHoursUsed
      );

      if (bestCandidate != null) {
        // Assign course to faculty
        Faculty bestFaculty = bestCandidate.faculty;
        facultyAssignments.computeIfAbsent(bestFaculty.getId(), k -> new ArrayList<>()).add(course);
        facultyHoursUsed.put(bestFaculty.getId(),
            facultyHoursUsed.getOrDefault(bestFaculty.getId(), 0) + course.getHoursRequiredPerWeek());
        assignedCourseIds.add(course.getId());
        
        // Update course's taughtBy relationship
        List<Faculty> currentTeachers = course.getTaughtBy() != null ? course.getTaughtBy() : new ArrayList<>();
        if (!currentTeachers.contains(bestFaculty)) {
          currentTeachers.add(bestFaculty);
          course.setTaughtBy(currentTeachers);
          courseRepository.save(course);
        }
      } else {
        // Criteria 1-3 failed, trigger LLM fallback for all interested faculties
        for (FacultyCandidate candidate : candidates) {
          kafkaProducer.sendLLMAssignmentEvent(
              candidate.faculty.getId(),
              course.getId(),
              termId,
              candidate.preferenceRank
          );
          log.info("Triggered LLM fallback for faculty {} and course {}",
              candidate.faculty.getId(), course.getId());
        }
        unassignedCourses.add(course);
      }
    }

    // Build response
    List<AssignmentResponse> responses = new ArrayList<>();
    
    // Get all faculties that had preferences
    Set<UUID> facultyIds = preferenceSets.stream()
        .map(set -> set.getFaculty().getId())
        .collect(Collectors.toSet());
    
    for (UUID facultyId : facultyIds) {
      Faculty faculty = facultyRepository.findById(facultyId).orElse(null);
      if (faculty == null) continue;

      List<Course> assigned = facultyAssignments.getOrDefault(facultyId, new ArrayList<>());
      List<AssignmentResponse.AssignedCourse> assignedCourses = assigned.stream()
          .map(c -> AssignmentResponse.AssignedCourse.builder()
              .courseId(c.getId())
              .courseCode(c.getCode())
              .courseName(c.getName())
              .hoursRequiredPerWeek(c.getHoursRequiredPerWeek())
              .build())
          .toList();

      responses.add(AssignmentResponse.builder()
          .facultyId(facultyId)
          .facultyName(faculty.getName())
          .assignedCourses(assignedCourses)
          .unassignedCourseIds(unassignedCourses.stream().map(Course::getId).toList())
          .build());
    }

    return responses;
  }

  private FacultyCandidate findBestFacultyCandidate(
      Course course,
      List<FacultyCandidate> candidates,
      Map<UUID, List<Course>> facultyAssignments,
      Map<UUID, Integer> facultyHoursUsed) {
    
    if (candidates.isEmpty()) {
      return null;
    }

    // Filter candidates that meet constraints
    List<FacultyCandidate> validCandidates = candidates.stream()
        .filter(candidate -> {
          Faculty faculty = candidate.faculty;
          
          // Check max 2 courses constraint
          List<Course> currentAssignments = facultyAssignments.getOrDefault(faculty.getId(), new ArrayList<>());
          if (currentAssignments.size() >= 2) {
            return false;
          }

          // Check max hours constraint
          int currentHours = facultyHoursUsed.getOrDefault(faculty.getId(), 0);
          if (currentHours + course.getHoursRequiredPerWeek() > faculty.getMaxHoursPerWeek()) {
            return false;
          }

          // Check if course is already assigned to this faculty
          if (course.getTaughtBy() != null && course.getTaughtBy().contains(faculty)) {
            return false;
          }

          return true;
        })
        .toList();

    if (validCandidates.isEmpty()) {
      return null;
    }

    // Rank by priority criteria
    validCandidates.sort((c1, c2) -> {
      Faculty f1 = c1.faculty;
      Faculty f2 = c2.faculty;

      // Priority 1: Seniority (lower seniorityScore = higher priority)
      int seniorityCompare = Integer.compare(f1.getSeniorityScore(), f2.getSeniorityScore());
      if (seniorityCompare != 0) {
        return seniorityCompare;
      }

      // Priority 2: Rating (higher rating = higher priority)
      int ratingCompare = Double.compare(f2.getRating(), f1.getRating());
      if (ratingCompare != 0) {
        return ratingCompare;
      }

      // Priority 3: Max hours available (more available = higher priority)
      int hours1 = f1.getMaxHoursPerWeek() - facultyHoursUsed.getOrDefault(f1.getId(), 0);
      int hours2 = f2.getMaxHoursPerWeek() - facultyHoursUsed.getOrDefault(f2.getId(), 0);
      int hoursCompare = Integer.compare(hours2, hours1);
      if (hoursCompare != 0) {
        return hoursCompare;
      }

      // Tie-breaker: Preference rank (lower rank = higher priority)
      return Integer.compare(c1.preferenceRank, c2.preferenceRank);
    });

    return validCandidates.get(0); // Return best candidate
  }

  // Helper class to track faculty candidates for a course
  private static class FacultyCandidate {
    final Faculty faculty;
    final UUID preferenceSetId;
    final Integer preferenceRank;

    FacultyCandidate(Faculty faculty, UUID preferenceSetId, Integer preferenceRank) {
      this.faculty = faculty;
      this.preferenceSetId = preferenceSetId;
      this.preferenceRank = preferenceRank;
    }
  }

  private Integer getPreferenceRank(UUID preferenceSetId, UUID courseId) {
    List<FacultyCoursePreference> preferences = facultyCoursePreferenceRepository
        .findByPreferenceSet_IdOrderByRankAsc(preferenceSetId);
    for (FacultyCoursePreference pref : preferences) {
      if (pref.getCourse().getId().equals(courseId)) {
        return pref.getRank();
      }
    }
    return null;
  }

  @Transactional(readOnly = true)
  public List<Course> getUnassignedCourses(UUID termId) {
    List<PreferenceSet> preferenceSets = preferenceSetRepository.findByTerm_Id(termId)
        .stream()
        .filter(set -> set.getStatus() == Status.CLOSED)
        .toList();

    Set<UUID> courseIds = new HashSet<>();
    for (PreferenceSet set : preferenceSets) {
      List<FacultyCoursePreference> preferences = facultyCoursePreferenceRepository
          .findByPreferenceSet_IdOrderByRankAsc(set.getId());
      for (FacultyCoursePreference pref : preferences) {
        courseIds.add(pref.getCourse().getId());
      }
    }

    List<Course> allCourses = courseRepository.findAllById(courseIds);
    return allCourses.stream()
        .filter(course -> course.getTaughtBy() == null || course.getTaughtBy().isEmpty())
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Course> getFacultyAssignments(UUID facultyId, UUID termId) {
    PreferenceSet preferenceSet = preferenceSetRepository.findByFaculty_IdAndTerm_Id(facultyId, termId)
        .orElse(null);
    
    if (preferenceSet == null) {
      return new ArrayList<>();
    }

    Faculty faculty = facultyRepository.findById(facultyId).orElse(null);
    if (faculty == null) {
      return new ArrayList<>();
    }

    // Get all courses this faculty teaches
    List<Course> allCourses = courseRepository.findAll();
    return allCourses.stream()
        .filter(course -> course.getTaughtBy() != null && course.getTaughtBy().contains(faculty))
        .toList();
  }

  @Transactional
  public void updateFacultyAssignments(UUID facultyId, List<UUID> courseIds) {
    Faculty faculty = facultyRepository.findById(facultyId)
        .orElseThrow(() -> new BadRequestException("Faculty not found"));

    // Validate courses exist
    List<Course> courses = courseRepository.findAllById(courseIds);
    if (courses.size() != new HashSet<>(courseIds).size()) {
      throw new BadRequestException("One or more course IDs do not exist");
    }

    // Check constraints
    if (courses.size() > 2) {
      throw new BadRequestException("Faculty cannot be assigned more than 2 courses");
    }

    int totalHours = courses.stream()
        .mapToInt(Course::getHoursRequiredPerWeek)
        .sum();
    if (totalHours > faculty.getMaxHoursPerWeek()) {
      throw new BadRequestException("Total hours exceed faculty's max hours per week");
    }

    // Remove faculty from all current course assignments
    List<Course> allCourses = courseRepository.findAll();
    for (Course course : allCourses) {
      if (course.getTaughtBy() != null && course.getTaughtBy().contains(faculty)) {
        course.getTaughtBy().remove(faculty);
        courseRepository.save(course);
      }
    }

    // Assign new courses
    for (Course course : courses) {
      List<Faculty> currentTeachers = course.getTaughtBy() != null ? course.getTaughtBy() : new ArrayList<>();
      if (!currentTeachers.contains(faculty)) {
        currentTeachers.add(faculty);
        course.setTaughtBy(currentTeachers);
        courseRepository.save(course);
      }
    }
  }

  @Transactional
  public void removeCourseAssignment(UUID facultyId, UUID courseId) {
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new BadRequestException("Course not found"));
    
    if (course.getTaughtBy() != null) {
      course.getTaughtBy().removeIf(f -> f.getId().equals(facultyId));
      courseRepository.save(course);
    }
  }

  @Transactional
  public void processLLMResult(UUID facultyId, UUID courseId, Boolean recommended) {
    if (!recommended) {
      return; // LLM doesn't recommend, leave unassigned
    }

    Faculty faculty = facultyRepository.findById(facultyId)
        .orElseThrow(() -> new BadRequestException("Faculty not found"));
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new BadRequestException("Course not found"));

    // Check constraints before assigning - get current assignments
    List<Course> allCourses = courseRepository.findAll();
    List<Course> currentAssignments = allCourses.stream()
        .filter(c -> c.getTaughtBy() != null && c.getTaughtBy().contains(faculty))
        .toList();
    
    if (currentAssignments.size() >= 2) {
      log.warn("Cannot assign course {} to faculty {}: already has 2 courses", courseId, facultyId);
      return;
    }

    int currentHours = currentAssignments.stream()
        .mapToInt(Course::getHoursRequiredPerWeek)
        .sum();
    if (currentHours + course.getHoursRequiredPerWeek() > faculty.getMaxHoursPerWeek()) {
      log.warn("Cannot assign course {} to faculty {}: exceeds max hours", courseId, facultyId);
      return;
    }

    // Assign course
    List<Faculty> currentTeachers = course.getTaughtBy() != null ? course.getTaughtBy() : new ArrayList<>();
    if (!currentTeachers.contains(faculty)) {
      currentTeachers.add(faculty);
      course.setTaughtBy(currentTeachers);
      courseRepository.save(course);
      log.info("Assigned course {} to faculty {} via LLM recommendation", courseId, facultyId);
    }
  }
}

