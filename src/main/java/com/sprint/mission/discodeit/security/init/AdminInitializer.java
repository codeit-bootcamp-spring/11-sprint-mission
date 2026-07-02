package com.sprint.mission.discodeit.security.init;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.username}")
  private String username;

  @Value("${admin.email}")
  private String email;

  @Value("${admin.password}")
  private String password;

  @Override
  public void run(ApplicationArguments args) {

    if (!userRepository.existsByRole(User.Role.ADMIN)) {

      User admin = User.create(username, email, passwordEncoder.encode(password));

      admin.updateRole(User.Role.ADMIN);

      userRepository.save(admin);
    }
  }
}
