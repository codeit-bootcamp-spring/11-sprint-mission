package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;

    public boolean isSelf(UUID userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
            return userId.equals(userDetails.getUserDto().id());
        }

        return userRepository.existsByIdAndUsername(userId, authentication.getName());
    }
}
