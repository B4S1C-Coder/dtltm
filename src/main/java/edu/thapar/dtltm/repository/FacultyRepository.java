package edu.thapar.dtltm.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.thapar.dtltm.model.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {

}
