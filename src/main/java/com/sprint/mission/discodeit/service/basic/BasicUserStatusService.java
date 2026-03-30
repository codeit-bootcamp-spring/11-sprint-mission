package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.CreateUserStatusRequestDTO;
import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusByUserIdResponseDTO;
import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusRequestDTO;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.entity.UserStatusType;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserStatus create(
            CreateUserStatusRequestDTO dto
    ) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userStatusRepository.findByUserId(user.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_STATUS_ALREADY_EXIST);
        }

        UserStatus newUserStatus = UserStatus.create(user.getId());

        return userStatusRepository.save(newUserStatus);
    }

    @Override
    public UserStatus find(
            UUID userStatusId
    ) {
        return  userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
    }

    @Override
    public List<UserStatus> findAll() {
        return userStatusRepository.findAll();
    }

    @Override
    public UserStatus update(
            UpdateUserStatusRequestDTO dto
    ) {
        UserStatus userStatus = find(dto.userStatusId());

        userStatus.updateLastOnlineTime();

        return userStatusRepository.save(userStatus);
    }

    @Override // 이거 그냥 온라인으로 강제로 업데이트하는 과정이라고 생각하자
    public UpdateUserStatusByUserIdResponseDTO updateByUserId(
            UUID userId
    ) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        userStatus.updateUserStatusType(UserStatusType.ONLINE);

        UpdateUserStatusByUserIdResponseDTO dto = UpdateUserStatusByUserIdResponseDTO.from(userStatusRepository.save(userStatus));

        return dto;
    }

    @Override
    public void delete(
            UUID userStatusId
    ) {
        UserStatus userStatus = find(userStatusId);

        userStatusRepository.deleteById(userStatus.getId());
    }
}
