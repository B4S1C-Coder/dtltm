package edu.thapar.dtltm.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "faculties")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Faculty {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotBlank
  private String name;

  @NotNull
  private Date dateOfJoin;

  // Lower the score, more senior the faculty
  @Builder.Default
  @Min(value = 1, message = "Minimum Seniority Score is 1.")
  @Max(value = 5, message = "Maximum Seniority Score is 5.")
  private Integer seniorityScore = 1;

  // Lower the score, lower the mobility
  @Builder.Default
  @Min(value = 1, message = "Minimum Mobility Score is 1.")
  @Max(value = 3, message = "Maximum Mobility Score is 3.")
  private Integer mobilityScore = 1;

  // Rating between 1.0 and 5.0, higher is better
  @Builder.Default
  @Min(value = 1, message = "Minimum Rating is 1.0.")
  @Max(value = 5, message = "Maximum Rating is 5.0.")
  private Double rating = 1.0;

  // Maximum hours per week the faculty can teach
  @Builder.Default
  @Min(value = 1, message = "Max hours per week must be at least 1.")
  private Integer maxHoursPerWeek = 20;

  @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  // CHECK: Can be rmeoved in favour of running query against course table and using join
  // While having this introduces redundancy, but reduces the need of above described join
  @ManyToMany(mappedBy = "taughtBy", fetch = FetchType.LAZY)
  private List<Course> courses;

}
