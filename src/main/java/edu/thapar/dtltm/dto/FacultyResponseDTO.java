package edu.thapar.dtltm.dto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class FacultyResponseDTO {
  private UUID id;
  private String name;
  private Date dateOfJoin;
  private Integer seniorityScore;
  private Integer mobilityScore;
  private Double rating;
  private Integer maxHoursPerWeek;
  private List<UUID> courses;
}
