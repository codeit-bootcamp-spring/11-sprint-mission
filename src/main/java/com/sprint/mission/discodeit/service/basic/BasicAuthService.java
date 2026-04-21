package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    public UserDto login(LoginRequest dto) {
        User user = userRepo.findByUsername(dto.username())
                .orElseThrow(() -> {
                    log.warn("Login failed. user not found. username={}", dto.username());
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        if(!user.getPassword().equals(dto.password())) {
            log.warn("Login failed. wrong password. username={}", dto.username());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        log.info("Login success. userId={}, username={}", user.getId(), user.getUsername());

        return userMapper.toDto(user);
    }
}
