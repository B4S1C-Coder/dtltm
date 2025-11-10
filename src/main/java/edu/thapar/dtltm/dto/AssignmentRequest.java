package edu.thapar.dtltm.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequest {
  @NotNull(message = "Term ID is required")
  private UUID termId;
}

