package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequestDto;
import com.sprint.mission.discodeit.dto.LoginResponseDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.InvalidPasswordException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
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
        List<User> userList = userRepo.findAll();
        for(User user : userList) {
            if(user.getName().equals(dto.name())) {
                if(user.getPassword().equals(dto.password())) {
                    return new LoginResponseDto(user.getId(), user.getName(), user.getEmail());
                } else {
                    throw new InvalidPasswordException();
                }
            }
        }
        throw new UserNotFoundException(dto.name());
    }
}
