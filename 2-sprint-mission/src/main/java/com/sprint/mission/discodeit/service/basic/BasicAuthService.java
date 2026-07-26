package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.UserUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtRegistry jwtRegistry;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void initAdmin(UserDto.CreateRequest request) {
    log.debug("어드민 계정 초기화 요청: username={}", request.username());
    UserDto.Response admin = userService.create(request, null);
    this.updateRoleInternal(new UserRoleUpdateRequest(admin.id(), Role.ADMIN));
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  @CacheEvict(cacheNames = "users", allEntries = true)
  public UserDto.Response updateRole(UserRoleUpdateRequest request) {
    log.debug("권한 수정 요청: userId={}, newRole={}", request.userId(), request.newRole());

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> UserNotFoundException.withId(request.userId()));
    Role oldRole = user.getRole();

    jwtRegistry.invalidateJwtInformationByUserId(request.userId());
    UserDto.Response response = applyRole(request);

    eventPublisher.publishEvent(new RoleUpdatedEvent(request.userId(), oldRole, request.newRole()));
    eventPublisher.publishEvent(new UserUpdatedEvent(response));

    return response;
  }

  private UserDto.Response updateRoleInternal(UserRoleUpdateRequest request) {
    log.debug("내부 권한 수정: userId={}, newRole={}", request.userId(), request.newRole());
    return applyRole(request);
  }

  private UserDto.Response applyRole(UserRoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> UserNotFoundException.withId(request.userId()));
    user.updateRole(request.newRole());
    log.info("사용자 권한 수정 완료: userId={}, newRole={}", request.userId(), request.newRole());
    return userMapper.toDto(user);
  }
}