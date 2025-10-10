package edu.thapar.dtltm.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.LoginResponseDTO;
import edu.thapar.dtltm.dto.UserRequestDTO;
import edu.thapar.dtltm.kafka.AuthServiceKafkaProducer;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.service.AuthService;
import edu.thapar.dtltm.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final AuthService authService;
  private final AuthServiceKafkaProducer kafkaProducer;

  @PostMapping("register")
  public ResponseEntity<?> register(@RequestBody UserRequestDTO userRequestDTO) {
    User user = userService.createUser(userRequestDTO);
    kafkaProducer.sendUserVerificationEvent(user);
    return ResponseEntity.ok("User created with email " + user.getEmail());
  }

  @PostMapping("login")
  public ResponseEntity<LoginResponseDTO> login(@RequestBody UserRequestDTO userRequestDTO) {
    Optional<String> token = authService.authenticate(userRequestDTO);

    if (!token.isPresent()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        new LoginResponseDTO(null, "Invalid Username or Password", false)
      );
    }

    return ResponseEntity.status(HttpStatus.OK).body(
      new LoginResponseDTO(token.get())
    );
  }
}
