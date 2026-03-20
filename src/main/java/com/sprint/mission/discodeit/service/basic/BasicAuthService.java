package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.user.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(request.getUsername())
                        && u.getPassword().equals(request.getPassword()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("username 또는 password가 올바르지 않습니다."));

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 UserStatus입니다."));
        userStatus.update(Instant.now());
        userStatusRepository.save(userStatus);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                userStatus.isOnline(),
                user.getProfileId(),
                user.getCreatedAt(),
                user.getUpdatedAt()
                );
    }
}
