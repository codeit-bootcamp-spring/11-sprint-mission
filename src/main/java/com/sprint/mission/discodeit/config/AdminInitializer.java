package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
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

  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_EMAIL = "admin@discodeit.com";
  private static final String ADMIN_PASSWORD = "admin1234";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    // 이미 ADMIN 사용자가 있으면 초기화하지 않음
    if (userRepository.existsByRole(Role.ADMIN)) {
      log.info("ADMIN 사용자 이미 존재함");
      return;
    }

    User admin = userRepository.findByUsername(ADMIN_USERNAME)
        .orElseGet(this::createAdminUser);

    // 기본 생성자는 USER 역할로 생성되므로 ADMIN으로 변경함
    admin.updateRole(Role.ADMIN);

    userRepository.save(admin);
    log.info("ADMIN 사용자 초기화 완료: username={}", admin.getUsername());
  }

  private User createAdminUser() {
    // 기본 관리자 계정 생성함
    String encodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
    User admin = new User(ADMIN_USERNAME, ADMIN_EMAIL, encodedPassword, null);

    // UserDto 변환 시 status가 필요하므로 함께 생성함
    new UserStatus(admin, Instant.now());

    return admin;
  }
}