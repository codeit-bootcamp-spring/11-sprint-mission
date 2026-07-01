package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final UserService userService;
  private final AuthService authService;

  @Value("${discodeit.admin.username}")
  private String adminUsername;

  @Value("${discodeit.admin.email}")
  private String adminEmail;

  @Value("${discodeit.admin.password}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) {
    UserDto.CreateRequest request = UserDto.CreateRequest.builder()
        .username(adminUsername)
        .email(adminEmail)
        .password(adminPassword)
        .build();
    try {
      UserDto.Response admin = userService.create(request, null);
      authService.updateRoleInternal(new UserRoleUpdateRequest(admin.id(), Role.ADMIN));
      log.info("어드민 계정 초기화 완료: username={}", adminUsername);
    } catch (UserAlreadyExistsException e) {
      log.warn("어드민 계정이 이미 존재합니다: username={}", adminUsername);
    } catch (Exception e) {
      log.error("어드민 계정 생성 중 오류가 발생: {}", e.getMessage());
    }
  }
}