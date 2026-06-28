package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.auth.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminInitializer implements ApplicationRunner {

  @Value("${discodeit.admin.username}")
  private String username;
  @Value("${discodeit.admin.email}")
  private String email;
  @Value("${discodeit.admin.password}")
  private String password;

  private final UserService userService;
  private final AuthService authService;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    UserCreateRequest request = new UserCreateRequest(username, email, password);
    try {
      UserResponse admin = userService.createUser(request, Optional.empty());
      authService.updateRole(new UserRoleUpdateRequest(admin.id(), Role.ADMIN));
      log.info("admin account initialized: username={}", admin.username());
    } catch (DuplicateUserException e) {
      log.debug("admin account already exists: username={}", username);
    } catch (Exception e) {
      log.error("admin account initialization failed: {}", e.getMessage(), e);
    }
  }
}
