package edu.thapar.dtltm.dto;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CourseUpdateDTO {
  private String name;
  private String code;
  
  @Min(value = 1, message = "Hours required per week must be at least 1.")
  private Integer hoursRequiredPerWeek;
  
  private List<UUID> taughtBy;
}

