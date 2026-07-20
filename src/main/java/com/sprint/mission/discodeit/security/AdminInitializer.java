package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicAuthService;
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
  @Value("${discodeit.admin.password}")
  private String password;
  @Value("${discodeit.admin.email}")
  private String email;
  private final UserService userService;
  private final BasicAuthService authService;

  @Override
  public void run(ApplicationArguments args) {
    // 관리자 계정 초기화 로직
    UserCreateRequest request = new UserCreateRequest(username, email, password);
    try {
      UserDto admin = userService.create(request, Optional.empty());
      authService.updateRoleInternal(new RoleUpdateRequest(admin.id(), Role.ADMIN));
      log.info("관리자 계정이 성공적으로 생성되었습니다.");
    } catch (UserAlreadyExistsException e) {
      log.warn("관리자 계정이 이미 존재합니다");
    }
  }
}
