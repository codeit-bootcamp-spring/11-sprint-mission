package com.sprint.mission.discodeit.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userSecurity")
public class UserSecurity {

    public boolean isSelf(UUID userId, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails)) {
            return false;
        }

        return userId.equals(userDetails.getUserDto().id());
    }
}
