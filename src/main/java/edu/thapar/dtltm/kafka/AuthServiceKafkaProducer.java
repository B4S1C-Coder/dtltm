package edu.thapar.dtltm.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import edu.thapar.dtltm.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceKafkaProducer {

  private final KafkaTemplate<String, String> emailVerificationTemplate;
  private static final String EMAIL_VERIFICATION = "user-registration";

  public void sendUserVerificationEvent(User user) {
    emailVerificationTemplate.send(EMAIL_VERIFICATION, user.getId().toString(), user.getEmail());
  }
}
