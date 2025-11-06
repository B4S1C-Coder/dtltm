package edu.thapar.dtltm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FacultyUpdateDTO {
  private String name;
  
  @Min(value = 1, message = "Minimum Seniority Score is 1.")
  @Max(value = 5, message = "Maximum Seniority Score is 5.")
  private Integer seniorityScore;
  
  @Min(value = 1, message = "Minimum Mobility Score is 1.")
  @Max(value = 3, message = "Maximum Mobility Score is 3.")
  private Integer mobilityScore;
  
  @Min(value = 1, message = "Minimum Rating is 1.0.")
  @Max(value = 5, message = "Maximum Rating is 5.0.")
  private Double rating;
  
  @Min(value = 1, message = "Max hours per week must be at least 1.")
  private Integer maxHoursPerWeek;
}

