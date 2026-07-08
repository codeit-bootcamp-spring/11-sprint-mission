package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.JwtRegistry;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserOnlineStatusMapper {

  private final JwtRegistry jwtRegistry;

  @Named("online")
  public Boolean online(User user) {
    // User가 없으면 온라인 상태가 아니라고 판단함
    if (user == null) {
      return false;
    }

    // JwtRegistry에 활성 JWT 정보가 있으면 로그인 중으로 판단함
    return jwtRegistry.hasActiveJwtInformationByUserId(user.getId());
  }
}