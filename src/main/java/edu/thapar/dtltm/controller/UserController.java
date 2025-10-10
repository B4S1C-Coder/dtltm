package edu.thapar.dtltm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.thapar.dtltm.dto.UserResponseDTO;
import edu.thapar.dtltm.mapper.UserMapper;
import edu.thapar.dtltm.model.User;

@RestController
@RequestMapping("/users")
public class UserController {

  @GetMapping("current-user-info")
  public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal User user) {
    return ResponseEntity.status(HttpStatus.OK).body(UserMapper.toDTO(user));
  }
}
