package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.AuthLoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.*;

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
            throw new ApiException(AUTH_USERNAME_REQUIRED);

        User user = this.userRepository.findByUsername(authLoginRequest.username())
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        if (authLoginRequest.password() == null || authLoginRequest.password().isBlank())
            throw new ApiException(AUTH_PASSWORD_REQUIRED);
        if (!authLoginRequest.password().equals(user.getPassword()))
            throw new ApiException(AUTH_INVALID_CREDENTIALS);

        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND));

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
