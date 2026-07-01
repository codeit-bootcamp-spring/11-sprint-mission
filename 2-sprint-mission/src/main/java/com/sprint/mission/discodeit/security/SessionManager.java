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

  private List<SessionInformation> findSessionsByUserId(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(principal -> principal instanceof DiscodeitUserDetails)
        .map(DiscodeitUserDetails.class::cast)
        .filter(details -> details.getUserDto().id().equals(userId))
        .flatMap(details -> sessionRegistry.getAllSessions(details, false).stream())
        .toList();
  }

  public void expireUserSessions(UUID userId) {
    findSessionsByUserId(userId).forEach(SessionInformation::expireNow);
  }

  public boolean hasActiveSessions(UUID userId) {
    return findSessionsByUserId(userId).stream()
        .anyMatch(session -> !session.isExpired());
  }
}