package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateParam;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusDto create(UserStatusCreateRequest request) {
        if (userRepository.findById(request.userId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (userStatusRepository.findByUserId(request.userId()).isPresent()) {
            throw new IllegalArgumentException("이미 해당 사용자의 UserStatus가 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(
                request.userId(),
                request.lastActiveAt()
        );

        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return new UserStatusDto(
                savedUserStatus.getId(),
                savedUserStatus.getUserId(),
                savedUserStatus.getLastActiveAt(),
                savedUserStatus.isOnline(),
                savedUserStatus.getCreatedAt(),
                savedUserStatus.getUpdatedAt()
        );
    }

    @Override
    public Optional<UserStatusDto> find(UUID id) {
        Optional<UserStatus> optionalUserStatus = userStatusRepository.findById(id);

        if (optionalUserStatus.isEmpty()) {
            return Optional.empty();
        }

        UserStatus userStatus = optionalUserStatus.get();

        UserStatusDto userStatusDto = new UserStatusDto(
                userStatus.getId(),
                userStatus.getUserId(),
                userStatus.getLastActiveAt(),
                userStatus.isOnline(),
                userStatus.getCreatedAt(),
                userStatus.getUpdatedAt()
        );

        return Optional.of(userStatusDto);
    }

    @Override
    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll().stream()
                .map(userStatus -> new UserStatusDto(
                        userStatus.getId(),
                        userStatus.getUserId(),
                        userStatus.getLastActiveAt(),
                        userStatus.isOnline(),
                        userStatus.getCreatedAt(),
                        userStatus.getUpdatedAt()
                ))
                .toList();
    }

    @Override
    public UserStatusDto update(UserStatusUpdateParam param) {
        UserStatus userStatus = userStatusRepository.findById(param.id())
                .orElseThrow(() -> new IllegalArgumentException("해당 UserStatus가 존재하지 않습니다."));

        userStatus.update(param.request().newLastActiveAt());

        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return new UserStatusDto(
                savedUserStatus.getId(),
                savedUserStatus.getUserId(),
                savedUserStatus.getLastActiveAt(),
                savedUserStatus.isOnline(),
                savedUserStatus.getCreatedAt(),
                savedUserStatus.getUpdatedAt()
        );
    }

    @Override
    public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 UserStatus가 존재하지 않습니다."));

        userStatus.update(request.newLastActiveAt());

        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return new UserStatusDto(
                savedUserStatus.getId(),
                savedUserStatus.getUserId(),
                savedUserStatus.getLastActiveAt(),
                savedUserStatus.isOnline(),
                savedUserStatus.getCreatedAt(),
                savedUserStatus.getUpdatedAt()
        );
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 UserStatus가 존재하지 않습니다."));

        userStatusRepository.delete(id);
    }
}