package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (userStatusRepository.existsByUserId(request.userId())) {
            throw new IllegalArgumentException("이미 해당 사용자의 상태 정보가 존재합니다.");
        }
        UserStatus userStatus = new UserStatus(request.userId());
        userStatusRepository.save(userStatus);
        return toDto(userStatus);
    }

    @Override
    public UserStatusDto findById(UUID id) {
        UserStatus userStatus = userStatusRepository.findByUserId(id).orElseThrow(
                () -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));
        return toDto(userStatus);
    }

    @Override
    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll().stream().
                map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void update(UUID id, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));
        userStatus.update();
        userStatusRepository.save(userStatus);
    }

    @Override
    public void updateByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId).orElseThrow(
                () -> new IllegalArgumentException("해당 사용자의 상태 정보를 찾을 수 없습니다."));
        userStatus.update();
        userStatusRepository.save(userStatus);
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.delete(id);
    }

    private UserStatusDto toDto(UserStatus userStatus) {
        return new UserStatusDto(userStatus.getId(), userStatus.getUserId(),
                userStatus.getCreatedAt(), userStatus.getUpdatedAt(),
                userStatus.isOnline());
    }
}
