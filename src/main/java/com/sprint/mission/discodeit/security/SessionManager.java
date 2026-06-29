package com.sprint.mission.discodeit.security;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionManager {

  private final SessionRegistry sessionRegistry;

  public List<SessionInformation> getActiveSessionByUserId(UUID userId) {
    for (Object principal : sessionRegistry.getAllPrincipals()) {
      if (principal instanceof DiscodeitUserDetails userDetails) {
        if (userDetails.getUserDto().id().equals(userId)) {
          return sessionRegistry.getAllSessions(principal, false);
        }
      }
    }
    return List.of();
  }

  public boolean isUserOnline(UUID userId) {
    return !getActiveSessionByUserId(userId).isEmpty();
  }
}
