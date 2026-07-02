package com.sprint.mission.discodeit.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SessionManager {

  private final SessionRegistry sessionRegistry;

  public boolean isOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(principal -> principal instanceof DiscodeitUserDetails userDetails
            && userDetails.getUser().id().equals(userId))
        .anyMatch(principal -> !sessionRegistry.getAllSessions(principal, false).isEmpty());
  }

  public void expireSessions(UUID userId) {
    sessionRegistry.getAllPrincipals().stream()
        .filter(principal -> principal instanceof DiscodeitUserDetails userDetails
            && userDetails.getUser().id().equals(userId))
        .forEach(p -> sessionRegistry.getAllSessions(p, false)
            .forEach(SessionInformation::expireNow));
  }
}
