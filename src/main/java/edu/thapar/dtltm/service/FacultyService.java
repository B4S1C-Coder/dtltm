package edu.thapar.dtltm.service;

import org.springframework.stereotype.Service;

import edu.thapar.dtltm.dto.FacultyRequestDTO;
import edu.thapar.dtltm.dto.UserRequestDTO;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FacultyService {
  private final FacultyRepository facultyRepository;
  private final UserService userService;

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
}
