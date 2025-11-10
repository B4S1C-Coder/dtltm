package edu.thapar.dtltm.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LLMResultRequest {
  @NotNull(message = "Faculty ID is required")
  private UUID facultyId;

  @NotNull(message = "Course ID is required")
  private UUID courseId;

  @NotNull(message = "Recommended flag is required")
  private Boolean recommended;
}

