package edu.thapar.dtltm.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Course {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotBlank
  @Column(unique = true)
  private String code;

  @NotBlank
  private String name;

  // Hours required per week for this course
  @Builder.Default
  @Min(value = 1, message = "Hours required per week must be at least 1.")
  private Integer hoursRequiredPerWeek = 3;
  
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "course_faculty",
    joinColumns = @JoinColumn(name = "course_id", nullable = false),
    inverseJoinColumns = @JoinColumn(name = "faculty_id", nullable = false)
  )
  private List<Faculty> taughtBy;
}
