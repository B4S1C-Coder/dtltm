package edu.thapar.dtltm.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.thapar.dtltm.dto.FacultyRequestDTO;
import edu.thapar.dtltm.dto.FacultyUpdateDTO;
import edu.thapar.dtltm.dto.UserRequestDTO;
import edu.thapar.dtltm.exception.ResourceNotFoundException;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FacultyService {
  private final FacultyRepository facultyRepository;
  private final UserService userService;

  @Transactional
  public Faculty createFaculty(FacultyRequestDTO facultyDTO) {
    // Build associated user class
    UserRequestDTO userRequestDTO = new UserRequestDTO(
      facultyDTO.getEmail(), facultyDTO.getPassword()
    );

    User associatedUser = userService.createUser(userRequestDTO);

    // Create the actual faculty
    return facultyRepository.save(
      Faculty.builder()
        .name(facultyDTO.getName())
        .dateOfJoin(facultyDTO.getDateOfJoin())
        .seniorityScore(facultyDTO.getSeniorityScore())
        .mobilityScore(facultyDTO.getMobilityScore())
        .user(associatedUser)
        .build()
    );
  }

  @Transactional(readOnly = true)
  public List<Faculty> getAllFaculties() {
    return facultyRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Faculty getFacultyById(UUID id) {
    return facultyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
  }

  @Transactional
  public Faculty updateFaculty(UUID id, FacultyUpdateDTO dto) {
    Faculty faculty = facultyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

    if (dto.getName() != null) {
      faculty.setName(dto.getName());
    }

    if (dto.getSeniorityScore() != null) {
      faculty.setSeniorityScore(dto.getSeniorityScore());
    }

    if (dto.getMobilityScore() != null) {
      faculty.setMobilityScore(dto.getMobilityScore());
    }

    if (dto.getRating() != null) {
      faculty.setRating(dto.getRating());
    }

    if (dto.getMaxHoursPerWeek() != null) {
      faculty.setMaxHoursPerWeek(dto.getMaxHoursPerWeek());
    }

    return facultyRepository.save(faculty);
  }

  @Transactional
  public void deleteFaculty(UUID id) {
    Faculty faculty = facultyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
    facultyRepository.delete(faculty);
  }
}
