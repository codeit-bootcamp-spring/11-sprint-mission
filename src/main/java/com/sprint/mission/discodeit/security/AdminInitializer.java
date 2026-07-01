package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.exception.service.user.NonExistUserException;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final JPAUserRepository userRepository;
  private final UserService userService;


  @Value("${admin.username:admin}")
  private String adminUsername;

  @Value("${admin.password}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) {
    // 이미 있으면 아무것도 안 함
    if (userRepository.existsByUsername(adminUsername)) {
      return;
    }

    userService.create(
        new UserCreateRequest(
            adminUsername,
            "discodeit@discodeit.com",
            adminPassword
        ),
        null
    );
    User admin = userRepository.findByUsername(adminUsername)
        .orElseThrow(() -> new NonExistUserException("name", adminUsername));
    admin.updateRole(Role.ADMIN);
    userRepository.save(admin);

  }
}