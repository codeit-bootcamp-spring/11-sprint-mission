package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.User;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserOnlineStatusResolver {

    private final SessionRegistry sessionRegistry;

    @Named("isOnline")
    public boolean isOnline(User user) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(DiscodeitUserDetails.class::isInstance)
                .map(DiscodeitUserDetails.class::cast)
                .filter(principal -> principal.getUserDto().id().equals(user.getId()))
                .anyMatch(principal ->
                        !sessionRegistry.getAllSessions(principal, false).isEmpty());
    }
}
