package edu.thapar.dtltm.dto;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FacultyRequestDTO {
  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 100, message = "Name length must belong to [3, 100].")
  private String name;

  @NotNull(message = "Date of joining is required")
  private Date dateOfJoin;

  @Min(value = 1, message = "Minimum Seniority Score is 1.")
  @Max(value = 5, message = "Maximum Seniority Score is 5.")
  private Integer seniorityScore = 1;

  @Min(value = 1, message = "Minimum Mobility Score is 1.")
  @Max(value = 3, message = "Maximum Mobility Score is 3.")
  private Integer mobilityScore = 1;

  @NotBlank(message = "Email is required")
  @Email(message = "Email address must be of a valid format")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be atleast 8 characters long")
  private String password;
}
