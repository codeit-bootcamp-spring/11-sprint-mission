package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.AuthLoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(AuthLoginRequest authLoginRequest) {
        if (authLoginRequest.username() == null || authLoginRequest.username().isBlank())
            throw new IllegalArgumentException("username is required. ❌");

        User user = this.userRepository.findByUsername(authLoginRequest.username())
                .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌"));

        if (authLoginRequest.password() == null || authLoginRequest.password().isBlank())
            throw new IllegalArgumentException("password is required. ❌");
        if (!authLoginRequest.password().equals(user.getPassword()))
            throw new IllegalArgumentException("password is not matched. ❌");

        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));

        status.setUpdatedAt();
        this.userStatusRepository.save(status);

        return this.toResponse(user, status);
    }

    private UserResponse toResponse(User user, UserStatus status) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileId(),
                new UserStatusResponse(
                        status.getUpdatedAt(),
                        status.getUpdatedAt().isAfter(Instant.now().minusSeconds(5 * 60))
                )
        );
    }
}
