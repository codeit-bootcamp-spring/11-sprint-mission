package com.sprint.mission.discodeit.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserOnlineStatusResolver {

    private final SessionRegistry sessionRegistry;

    public Set<UUID> getOnlineUserIds() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(DiscodeitUserDetails.class::isInstance)
                .map(DiscodeitUserDetails.class::cast)
                .filter(principal ->
                        !sessionRegistry.getAllSessions(principal, false)
                                .isEmpty()
                )
                .map(principal -> principal.getUserDto().id())
                .collect(Collectors.toSet());
    }

    public void expireSessions(UUID userId) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(DiscodeitUserDetails.class::isInstance)
                .map(DiscodeitUserDetails.class::cast)
                .filter(principal ->
                        principal.getUserDto().id().equals(userId))
                .flatMap(principal ->
                        sessionRegistry
                                .getAllSessions(principal, false)
                                .stream())
                .forEach(SessionInformation::expireNow);
    }
}
