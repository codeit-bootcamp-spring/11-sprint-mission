package com.sprint.mission.discodeit.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserOnlineStatusProvider {

  private final SessionRegistry sessionRegistry;

  public boolean isOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(principal -> principal instanceof DiscodeitUserDetails)
        .map(principal -> (DiscodeitUserDetails) principal)
        .filter(userDetails -> userId.equals(userDetails.getUserDto().id()))
        .flatMap(userDetails -> sessionRegistry.getAllSessions(userDetails, false).stream())
        .anyMatch(session -> !session.isExpired());
  }
}