package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequestDTO;
import com.sprint.mission.discodeit.dto.auth.LoginResponseDTO;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.entity.UserStatusType;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public LoginResponseDTO login(
            LoginRequestDTO dto
    ) {
        // 검증
        // - username 기반 User가 있는지 확인 (즉, username이 맞는지 검증)
        User targetUser = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // - 비밀번호 검증
        targetUser.authenticate(dto.password());

        // 유저 상태
        // - 유저 상태 조회
        UserStatus userStatus = userStatusRepository.findByUserId(targetUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        // - 유저 상태 온라인으로 업데이트
        userStatus.updateUserStatusType(UserStatusType.ONLINE);
        userStatusRepository.save(userStatus);

        return LoginResponseDTO.from(targetUser, userStatus);
    }
}

// 아이디(username) 혹은 비밀번호 둘 중 뭐가 틀렸는지 알려주지 않음 (ErrorCode.INVALID_CREDENTIALS)
