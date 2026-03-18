package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;

    @Override
    public UserResponse login(LoginRequest request) {
        return userRepository.readAll().stream()
                .filter(u -> u.getUserName().equals(request.getUserName()) && u.getUserPassword().equals(request.getUserPassword()))
                .findFirst()
                .map(u -> new UserResponse(u.getId(), u.getUserName(), u.getUserEmail(), true))
                .orElseThrow(() -> new IllegalArgumentException("아이디 비밀번호가 일치하지 않습니다."));
    }
}
