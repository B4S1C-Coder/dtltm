package edu.thapar.dtltm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.FacultyRequestDTO;
import edu.thapar.dtltm.kafka.AuthServiceKafkaProducer;
import edu.thapar.dtltm.model.Faculty;
import edu.thapar.dtltm.service.FacultyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/faculty")
@RequiredArgsConstructor
public class FacultyController {
  
  private final FacultyService facultyService;
  private final AuthServiceKafkaProducer kafkaProducer;

  @PostMapping("create")
  public ResponseEntity<?> createFaculty(@RequestBody FacultyRequestDTO facultyDto) {
    Faculty faculty = facultyService.createFaculty(facultyDto);
    kafkaProducer.sendUserVerificationEvent(faculty.getUser());
    return ResponseEntity.ok("Created faculty with email: " + faculty.getUser().getEmail());
  }

}
