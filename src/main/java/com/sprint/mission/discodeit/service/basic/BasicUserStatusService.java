package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.userStatus.CreateUserStatusRequest;
import com.sprint.mission.discodeit.dto.request.userStatus.UpdateUserStatusRequest;
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
    public UserStatus createUserStatus(CreateUserStatusRequest request) {
        if (userRepository.findById(request.getUserId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 User입니다.");
        }
        if (userStatusRepository.findByUserId(request.getUserId()) != null) {
            throw new IllegalArgumentException("이미 존재하는 UserStatus입니다.");
        }
        UserStatus userStatus = new UserStatus(request.getUserId());
        userStatusRepository.save(userStatus);
        return userStatus;
    }

    @Override
    public UserStatus getUserStatusById(UUID id) {
        return userStatusRepository.findById(id);
    }

    @Override
    public List<UserStatus> getAllUserStatuses() {
        return userStatusRepository.findAll();
    }

    @Override
    public void updateUserStatus(UUID id, UpdateUserStatusRequest request) {
        UserStatus userStatus = userStatusRepository.findById(id);
        if (userStatus == null) {
            throw new IllegalArgumentException("존재하지 않는 UserStatus입니다.");
        }
        userStatus.update(request.getLastActiveAt());
        userStatusRepository.save(userStatus);
    }

    @Override
    public void updateUserStatusByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId);
        if (userStatus == null) {
            throw new IllegalArgumentException("존재하지 않는 UserStatus입니다.");
        }
        userStatus.update(Instant.now());
        userStatusRepository.save(userStatus);
    }

    @Override
    public void deleteUserStatus(UUID id) {
        userStatusRepository.deleteById(id);
    }
}