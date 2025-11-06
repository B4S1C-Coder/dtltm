package edu.thapar.dtltm.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.thapar.dtltm.dto.CourseCreationDTO;
import edu.thapar.dtltm.exception.BadRequestException;
import edu.thapar.dtltm.exception.ConflictException;
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
}
