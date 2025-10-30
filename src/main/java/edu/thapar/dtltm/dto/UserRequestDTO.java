package edu.thapar.dtltm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRequestDTO {
  @NotBlank(message = "Email is required")
  @Email(message = "Email address must be of a valid format")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be atleast 8 characters long")
  private String password;
}
