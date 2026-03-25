package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatus create(UserStatusCreateDto dto) {
        // 관련된 User가 존재하지 않으면 예외를 발생
        userRepository.findById(dto.userId());

        // 같은 User와 관련된 객체가 이미 존재하면 예외를 발생
        if (userRepository.findById(dto.userId()) == null) {
            throw new IllegalArgumentException("이미 존재하는 UserStatus입니다. userId: " + dto.userId());
        }

        UserStatus userStatus = new UserStatus(dto.userId(), Instant.now());
        userStatusRepository.insert(userStatus);

        return userStatus;
    }

    @Override
    public UserStatus find(UUID id) {
        return userStatusRepository.findById(id);
    }

    @Override
    public List<UserStatus> findAll() {
        return userStatusRepository.findAll();
    }

    @Override
    public UserStatus update(UUID id, UserStatusUpdateDto dto) {
        UserStatus userStatus = userStatusRepository.findById(id);
        userStatus.updateLastOnline();
        userStatusRepository.update(userStatus);

        return userStatus;
    }

    @Override
    public UserStatus updateByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId);
        userStatus.updateLastOnline();
        userStatusRepository.update(userStatus);

        return userStatus;
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.delete(id);
    }
}