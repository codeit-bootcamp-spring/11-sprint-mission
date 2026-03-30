package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.InvalidAuthRequestException;
import com.sprint.mission.discodeit.exception.auth.LoginFailedException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("service-basic")
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    @Transactional
    public UserResponse login(LoginRequest request) {
        if (request.getUserName() == null || request.getUserName().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidAuthRequestException("아이디와 비밀번호를 모두 입력해주세요.");
        }

        User user = userRepository.findAll().stream()
                .filter(u -> u.getUserName().equals(request.getUserName())
                        && u.getPassword().equals(request.getPassword()))
                .findFirst()
                .orElseThrow(() -> new LoginFailedException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 온라인 상태 확인
        UserStatus status = userStatusRepository.findById(user.getId());
        boolean isOnline = (status != null) && status.isOnline();

        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getProfileId(),
                isOnline,
                user.getJoinedChannelId()
        );
    }
}