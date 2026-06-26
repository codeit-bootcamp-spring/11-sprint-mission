package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    String adminUsername = "admin";

    if (!userRepository.existsByUsername(adminUsername)) {
      log.info("어드민 계정이 존재하지 않아 초기화를 진행합니다.");

      String encodedPassword = passwordEncoder.encode("admin1234!");
      User admin = new User(adminUsername, "admin@discodeit.com", encodedPassword);

      admin.updateRole(Role.ADMIN);

      userRepository.save(admin);
      log.info("어드민 계정 초기화 성공 - username: {}, email: admin@discodeit.com", adminUsername);
    } else {
      log.info("어드민 계정이 이미 존재하므로 초기화를 건너뜁니다.");
    }
  }
}
