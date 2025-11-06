package edu.thapar.dtltm.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.thapar.dtltm.model.Course;

public interface CourseRepository extends JpaRepository<Course, UUID> {
  boolean existsByCode(String code);
}
