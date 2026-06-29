package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.account.username}")
  private String adminUsername;

  @Value("${admin.account.email}")
  private String adminEmail;

  @Value("${admin.account.password}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (!userRepository.existsByUsernameOrEmail("admin", "admin@discodeit.com")) {
      String encodedAdminPassword = passwordEncoder.encode(adminPassword);
      User admin = new User(adminUsername, adminEmail, encodedAdminPassword, null);
      admin.updateRole(Role.ADMIN);
      userRepository.save(admin);
      log.debug("admin 계정 생성 완료");
    }
  }
}