package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public UserStatusDto create(UserStatusCreateRequest dto) {
        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(userStatusRepo.findByUser(user).isPresent())
            throw new BusinessException(ErrorCode.USER_STATUS_ALREADY_EXISTS);

        UserStatus userStatus = new UserStatus(user, dto.lastActiveAt());
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
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserStatus userStatus = userStatusRepo.findByUser(user)
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
    @Transactional
    public void update(UUID id, UserStatusUpdateRequest dto) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        userStatus.update(dto.newLastActiveAt());
    }

    @Override
    @Transactional
    public void updateByUserId(UUID id, UserStatusUpdateRequest dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserStatus userStatus = userStatusRepo.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        userStatus.update(dto.newLastActiveAt());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
        userStatusRepo.delete(userStatus);
    }

    private UserStatusDto toDto(UserStatus userStatus) {
        return new UserStatusDto(
                userStatus.getId(),
                userStatus.getUser().getId(),
                userStatus.getLastActiveAt()
        );
    }
}
