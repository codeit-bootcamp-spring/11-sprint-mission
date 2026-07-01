package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
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
  public void run(ApplicationArguments args) {
    try {
      UserDto admin = userService.create(
          new UserCreateRequest(username, email, password),
          Optional.empty()
      );
      authService.updateRoleInternal(new UserRoleUpdateRequest(admin.id(), Role.ADMIN));
      log.info("어드민 계정 초기화 완료: username={}", username);
    } catch (UserAlreadyExistsException e) {
      log.info("어드민 계정이 이미 존재합니다. 초기화를 건너뜁니다.");
    } catch (Exception e) {
      log.error("어드민 계정 초기화 중 오류 발생: {}", e.getMessage());
    }
  }
}
