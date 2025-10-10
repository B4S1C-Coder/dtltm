package edu.thapar.dtltm.util;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.thapar.dtltm.model.User;

public class SecurityUtil {
  public static Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof User) {
      return Optional.of((User) authentication.getPrincipal());
    }

    return Optional.empty();
  }
}
