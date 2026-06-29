package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.UserException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.auth.PreAuthorizeStaticParam.ROLE_ADMIN;
import static com.sprint.mission.discodeit.auth.PreAuthorizeStaticParam.ROLE_MANAGER;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final SessionRegistry sessionRegistry;

  @Override
  @Transactional
  @PreAuthorize(ROLE_ADMIN)
  public UserDto updateUserRole(UserRoleUpdateRequest request) {
    User foundUser = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

    foundUser.updateRole(request.newRole());

    // 권한이 변경된 사용자가 현재 로그인 상태라면 세션 무효화
    invalidateUserSession(request.userId());

    return UserDto.from(foundUser);
  }

  private void invalidateUserSession(UUID userId) {
    List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

    for (Object principal : allPrincipals) {
      if (principal instanceof DiscodeitUserDetails userDetails) {
        if (userDetails.getUserDto().id().equals(userId)) {
          // 해당 유저의 모든 세션 정보를 가져옴(false: 만료된 세션 제외)
          List<SessionInformation> sessions = sessionRegistry.getAllSessions(userDetails, false);
          for (SessionInformation session : sessions) {
            session.expireNow(); // 즉시 세션 만료 처리!
          }
        }
      }
    }
  }


}
