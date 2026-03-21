package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.AuthLoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        User user = this.userRepository.findByUsername(authLoginRequest.username());

        if (authLoginRequest.password() == null || authLoginRequest.password().isBlank())
            throw new IllegalArgumentException("password is required. ❌");
        if (!authLoginRequest.password().equals(user.getPassword()))
            throw new IllegalArgumentException("password is not matched. ❌");

        BinaryContent profile = user.getProfileId() != null
                ? this.binaryContentRepository.findById(user.getProfileId())
                : null;

        UserStatus status  = this.userStatusRepository.findByUserId(user.getId());
        status.setUpdatedAt();
        this.userStatusRepository.save(status);

        return user.toResponse(profile, status);
    }
}
