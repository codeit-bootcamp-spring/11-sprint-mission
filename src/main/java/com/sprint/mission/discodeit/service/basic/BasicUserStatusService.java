package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
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

    private final UserStatusRepository userStatusRepo;
    private final UserRepository userRepo;

    @Override
    public UserStatusDto create(UserStatusCreateRequest dto) {
        userRepo.findById(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean exists = userStatusRepo.findAll().stream()
                .anyMatch(p -> p.getUserId().equals(dto.userId()));
        if(exists) throw new BusinessException(ErrorCode.USER_STATUS_ALREADY_EXISTS);

        UserStatus userStatus = new UserStatus(dto.userId(), dto.lastActiveAt());
        userStatusRepo.save(userStatus);
        return toDto(userStatus);
    }

    @Override
    public UserStatusDto find(UUID id) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        return toDto(userStatus);
    }

    @Override
    public UserStatusDto findByUserId(UUID id) {
        userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserStatus userStatus = userStatusRepo.findByUserId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        return toDto(userStatus);
    }

    @Override
    public List<UserStatusDto> findAll() {
        return userStatusRepo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void update(UUID id, UserStatusUpdateRequest dto) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        userStatus.setLastActiveAt(dto.newLastActiveAt());
        userStatus.update();
        userStatusRepo.save(userStatus);
    }

    @Override
    public void updateByUserId(UUID id, UserStatusUpdateRequest dto) {
        userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserStatus userStatus = userStatusRepo.findByUserId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        userStatus.setLastActiveAt(dto.newLastActiveAt());
        userStatus.update();
        userStatusRepo.save(userStatus);
    }

    @Override
    public void delete(UUID id) {
        userStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
        userStatusRepo.deleteById(id);
    }

    private UserStatusDto toDto(UserStatus userStatus) {
        return new UserStatusDto(userStatus.getId(), userStatus.getUserId(), userStatus.getLastActiveAt());
    }
}
