package edu.thapar.dtltm.mapper;

import edu.thapar.dtltm.dto.UserResponseDTO;
import edu.thapar.dtltm.model.User;

public class UserMapper {
  public static UserResponseDTO toDTO(User user) {
    UserResponseDTO userResponseDTO = new UserResponseDTO();
    userResponseDTO.setEmail(user.getEmail());
    userResponseDTO.setRole(user.getRole());
    return userResponseDTO;
  }
}
