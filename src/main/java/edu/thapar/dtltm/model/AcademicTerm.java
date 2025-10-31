package edu.thapar.dtltm.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(
  name = "academic_terms",
  uniqueConstraints = @UniqueConstraint(columnNames = {"year", "season"})
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcademicTerm {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotNull
  @Min(2000)
  @Max(2100)
  @Column(nullable = false)
  private Integer year;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Season season;

  public enum Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER
  }
}


