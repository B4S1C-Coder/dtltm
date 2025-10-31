package edu.thapar.dtltm.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(
  name = "faculty_course_preferences",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"preference_set_id", "rank"}),
    @UniqueConstraint(columnNames = {"preference_set_id", "course_id"})
  }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacultyCoursePreference {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "preference_set_id", nullable = false)
  private PreferenceSet preferenceSet;

  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @NotNull
  @Min(1)
  @Column(nullable = false)
  private Integer rank; // 1..N, strict order
}


