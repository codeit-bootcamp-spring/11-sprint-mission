package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    public UserDto login(UUID id, LoginRequest loginRequest) {
        String name = loginRequest.username();
        String password = loginRequest.password();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로그인 할 유저가 존재하지 않습니다."));
        if(user.getUsername().equals(name) && user.getPassword().equals(password)) {
            UserStatus status = userStatusRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("상태 정보를 찾을 수 없습니다."));
            return UserDto.from(user, status);
        } else {
            throw new IllegalArgumentException("로그인 정보가 맞지 않습니다.");
        }
    }
}
