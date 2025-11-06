package edu.thapar.dtltm.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.thapar.dtltm.dto.CourseCreationDTO;
import edu.thapar.dtltm.dto.CourseUpdateDTO;
import edu.thapar.dtltm.exception.BadRequestException;
import edu.thapar.dtltm.exception.ConflictException;
import edu.thapar.dtltm.exception.ResourceNotFoundException;
import edu.thapar.dtltm.model.Course;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.repository.CourseRepository;
import edu.thapar.dtltm.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;
  private final FacultyRepository facultyRepository;

  @Transactional
  public Course createCourse(CourseCreationDTO dto) {
    // Uniqueness of course code
    if (courseRepository.existsByCode(dto.getCode())) {
      throw new ConflictException(
        "Course with code: " + dto.getCode() + " already exists."
      );
    }

    // Validate faculty ids
    List<Faculty> faculities = null;
    if (dto.getTaughtBy() != null && !dto.getTaughtBy().isEmpty()) {
      faculities = facultyRepository.findAllById(dto.getTaughtBy());

      if (faculities.size() != new HashSet<>(dto.getTaughtBy()).size()) {
        throw new BadRequestException("One or more faculty IDs do not exist");
      }
    } else {
      faculities = List.of();
    }

    Course course = Course.builder()
      .code(dto.getCode())
      .name(dto.getName())
      .taughtBy(faculities)
      .build();
    
    return courseRepository.save(course);
  }

  @Transactional(readOnly = true)
  public List<Course> getAllCourses() {
    return courseRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Course getCourseById(UUID id) {
    return courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
  }

  @Transactional
  public Course updateCourse(UUID id, CourseUpdateDTO dto) {
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

    // Update code if provided and check uniqueness
    if (dto.getCode() != null && !dto.getCode().equals(course.getCode())) {
      if (courseRepository.existsByCode(dto.getCode())) {
        throw new ConflictException("Course with code: " + dto.getCode() + " already exists.");
      }
      course.setCode(dto.getCode());
    }

    // Update name if provided
    if (dto.getName() != null) {
      course.setName(dto.getName());
    }

    // Update hours required per week if provided
    if (dto.getHoursRequiredPerWeek() != null) {
      course.setHoursRequiredPerWeek(dto.getHoursRequiredPerWeek());
    }

    // Update taughtBy if provided
    if (dto.getTaughtBy() != null) {
      List<Faculty> faculties = facultyRepository.findAllById(dto.getTaughtBy());
      if (faculties.size() != new HashSet<>(dto.getTaughtBy()).size()) {
        throw new BadRequestException("One or more faculty IDs do not exist");
      }
      course.setTaughtBy(faculties);
    }

    return courseRepository.save(course);
  }

  @Transactional
  public void deleteCourse(UUID id) {
    Course course = courseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    courseRepository.delete(course);
  }
}
