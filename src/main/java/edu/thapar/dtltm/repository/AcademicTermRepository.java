package edu.thapar.dtltm.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.thapar.dtltm.model.AcademicTerm;
import edu.thapar.dtltm.model.AcademicTerm.Season;

public interface AcademicTermRepository extends JpaRepository<AcademicTerm, UUID> {
  Optional<AcademicTerm> findByYearAndSeason(Integer year, Season season);
}


