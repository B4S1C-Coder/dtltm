package edu.thapar.dtltm.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.thapar.dtltm.dto.UserRequestDTO;
import edu.thapar.dtltm.util.JwtUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public Optional<String> authenticate(UserRequestDTO userRequestDTO) {
    Optional<String> token = userService.findByEmail(userRequestDTO.getEmail())
      .filter(u -> passwordEncoder.matches(userRequestDTO.getPassword(), u.getPassword()))
      .map(u -> jwtUtil.generateToken(u.getId().toString(), u.getRole()));

    return token;
  }
}
