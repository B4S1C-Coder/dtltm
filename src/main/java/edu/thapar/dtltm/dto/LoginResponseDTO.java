package edu.thapar.dtltm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
  private final String token;
  private String message;
  private Boolean success = true;
}
