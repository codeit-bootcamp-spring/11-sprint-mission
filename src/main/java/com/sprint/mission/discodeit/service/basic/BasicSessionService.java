package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import com.sprint.mission.discodeit.service.SessionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicSessionService implements SessionService {

  private final JwtRegistry jwtRegistry;

  @Override
  public boolean isOnline(UUID userId) {

    // 사용자가 Registry에 등록되었는지 확인(로그인 여부 확인)
    return jwtRegistry.hasActiveJwtInformationByUserId(userId);
  }

}
