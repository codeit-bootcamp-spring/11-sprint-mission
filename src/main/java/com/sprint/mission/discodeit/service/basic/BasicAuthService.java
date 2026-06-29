package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.SessionManager;
import com.sprint.mission.discodeit.security.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final SessionManager sessionManager;

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    user.updateRole(request.newRole());

    sessionManager.getActiveSessionByUserId(user.getId())
        .forEach(SessionInformation::expireNow);
    log.debug("사용자 권한 수정 완료 - userId: {}, newRole: {}", request.userId(), request.newRole());

    return userMapper.toDto(user);
  }

//
//  @Override
//  public UserDto login(LoginRequest dto) {
//    log.debug("login 시도 - username: {}", dto.username()); // 보안을 위해 유저의 이름만 기록
//
//    // dto에서 가져온 이름으로 찾은 유저가 있다면, 유저 비밀번호와 일치하는지 확인
//    User user = userRepository.findByUsername(dto.username())
//        .orElseThrow(
//            () -> new BadCredentialsException(dto.username()));
//
//    if (!user.getPassword().equals(dto.password())) {
//      throw new BadCredentialsException(dto.username());
//    }
//
//    UserStatus status = user.getStatus();
//    status.updateTime(Instant.now());
//
//    log.info("로그인 성공 - userId: {}", user.getId());
//    return userMapper.toDto(user);
//  }
}
