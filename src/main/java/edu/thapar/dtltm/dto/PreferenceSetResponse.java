package edu.thapar.dtltm.dto;

import java.util.List;
import java.util.UUID;

import edu.thapar.dtltm.model.AcademicTerm;
import edu.thapar.dtltm.model.PreferenceSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreferenceSetResponse {
  private UUID id;
  private UUID facultyId;
  private Term term;
  private List<UUID> candidateCourseIds;
  private List<Preference> preferences;
  private PreferenceSet.Status status;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Term {
    private Integer year;
    private AcademicTerm.Season season;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Preference {
    private UUID courseId;
    private Integer rank;
  }
}


