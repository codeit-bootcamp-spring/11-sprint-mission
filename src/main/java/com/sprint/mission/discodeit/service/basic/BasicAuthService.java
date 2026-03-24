package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.login.LoginRequestDto;
import com.sprint.mission.discodeit.dto.login.LoginResponseDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.login.InvalidPasswordException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepo;

    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepo.findByName(dto.name())
                .orElseThrow(() -> new UserNotFoundException(dto.name()));

        if(!user.getPassword().equals(dto.password())) {
            throw new InvalidPasswordException();
        }

        return new LoginResponseDto(user.getId(), user.getName(), user.getEmail());
    }
}
