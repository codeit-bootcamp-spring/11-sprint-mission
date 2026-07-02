package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.SessionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicSessionService implements SessionService {

  private final SessionRegistry sessionRegistry;

  @Override
  public boolean isOnline(UUID userId) {

    // 현재 로그인 중인 모든 사용자를 조회
    return sessionRegistry.getAllPrincipals().stream()
        // DiscodeitUserDetails 타입만 남김
        .filter(principal -> principal instanceof DiscodeitUserDetails)
        // object를 userDetails로 변환
        .map(principal -> (DiscodeitUserDetails) principal)
        // 사용자의 id가 일치하고 사용자의 모든 세션을 찾았을때 비어있지 않으면 true -> online
        .anyMatch(userDetails ->
            userDetails.getUserDto().id().equals(userId)
                && !sessionRegistry.getAllSessions(userDetails, false).isEmpty()
        );
  }

}
