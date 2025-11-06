package edu.thapar.dtltm.dto;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAssignmentRequest {
  @NotNull(message = "Course IDs are required")
  private List<UUID> courseIds;
}

