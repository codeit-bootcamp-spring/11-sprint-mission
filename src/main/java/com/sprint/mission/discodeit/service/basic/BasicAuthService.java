package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  private final SessionRegistry sessionRegistry;

  // Role Update
  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest dto) {
    log.debug("[USER_ROLE_UPTATE_START] 유저 권한 수정 시작 - 권한 수정할 유저 ID={}", dto.userId());

    User user = userRepository.findById(dto.userId()).orElseThrow(
        () -> new UserNotFoundException(dto.userId())
    );

    user.updateRole(dto.newRole());

    // 현재 로그인 중인 모든 사용자를 조회
    sessionRegistry.getAllPrincipals()
        .stream()
        // DiscodeitUserDetails 타입만 남기고
        // 특정 userId를 가진 유저를 조회 → 여기서는 권한 수정할 유저를 조회
        // 로그인 중인 사용자 id가 a, b, c, d면 a==dto.userId(), ..., d==dto.userId()
        .filter(principal ->
            principal instanceof DiscodeitUserDetails userDetails &&
                userDetails.getUserDto().id().equals(dto.userId()))
        // filter 조건에 맞는 사용자의 모든 세션을 찾아 인증 무효화(세션 만료X)
        .forEach(principal -> {
          sessionRegistry.getAllSessions(principal, false)
              .forEach(SessionInformation::expireNow);
        });

    log.info("[USER_ROLE_UPDATE_SUCCESS] 유저 권한 수정 완료 - 권한 수정한 유저 ID={}", dto.userId());

    return userMapper.toDto(user);
  }
}
