package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.AuthDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserDto.Response login(AuthDto.LoginRequest request) {
        User user = userRepository.findByName(request.username())
                .orElseThrow(() -> new NoSuchElementException("Invalid username or password"));

        // 비밀번호 암호화 미구현
        if (!user.getPassword().equals(request.password())) {
            throw new NoSuchElementException("Invalid username or password");
        }

                UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NoSuchElementException("User status information not found"));


        return UserDto.Response.of(user, userStatus);
    }
}