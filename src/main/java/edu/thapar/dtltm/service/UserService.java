package edu.thapar.dtltm.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.thapar.dtltm.dto.UserRequestDTO;
import edu.thapar.dtltm.model.User;
import edu.thapar.dtltm.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User createUser(UserRequestDTO userRequestDTO) {
    String email = userRequestDTO.getEmail();

    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isPresent()) {
      throw new ResponseStatusException(
        HttpStatus.CONFLICT, "User with email: " + email + "already exists"
      );
    }

    return userRepository.save(
      User.builder()
        .email(email)
        .password(passwordEncoder.encode(userRequestDTO.getPassword()))
        .build()
    );
  }
}
