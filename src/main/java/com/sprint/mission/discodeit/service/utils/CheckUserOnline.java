package com.sprint.mission.discodeit.service.utils;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CheckUserOnline {

    private final SessionRegistry sessionRegistry;

    public boolean isUserOnline(UUID userId) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof DiscodeitUserDetails)
                .map(principal -> (DiscodeitUserDetails) principal)
                // 현재 접속 중인 유저 중 ID가 일치하고, 만료되지 않은 세션이 1개 이상 존재하는지 검사
                .anyMatch(userDetails -> userDetails.getUserDto().id().equals(userId)
                        && !sessionRegistry.getAllSessions(userDetails, false).isEmpty());
    }
}
