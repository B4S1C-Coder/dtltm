package edu.thapar.dtltm.mapper;

import edu.thapar.dtltm.dto.FacultyResponseDTO;
import edu.thapar.dtltm.model.Faculty;

public class FacultyMapper {
  public static FacultyResponseDTO toDTO(Faculty faculty) {
    FacultyResponseDTO dto = new FacultyResponseDTO();
    dto.setId(faculty.getId());
    dto.setName(faculty.getName());
    dto.setDateOfJoin(faculty.getDateOfJoin());
    dto.setMaxHoursPerWeek(faculty.getMaxHoursPerWeek());
    dto.setMobilityScore(faculty.getMobilityScore());
    dto.setSeniorityScore(faculty.getSeniorityScore());
    dto.setRating(faculty.getRating());
    dto.setCourses(faculty.getCourses().stream().map(c -> c.getId()).toList());

    return dto;
  }
}
