package edu.thapar.dtltm.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseCreationDTO {
  @NotBlank(message = "code i.e. course code is required")
  private String code;

  @NotBlank(message = "Name of the course is required")
  private String name;

  private List<UUID> taughtBy = new ArrayList<>();

}
