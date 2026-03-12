package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.UserStatus;
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

    @Override
    public UserStatus createUserStatus(UUID userId) {
        UserStatus userStatus = new UserStatus(userId);
        userStatusRepository.save(userStatus);
        return userStatus;
    }

    @Override
    public UserStatus getUserStatusById(UUID id) {
        return userStatusRepository.findById(id);
    }

    @Override
    public UserStatus getUserStatusByUserId(UUID userId) {
        return userStatusRepository.findByUserId(userId);
    }

    @Override
    public List<UserStatus> getAllUserStatuses() {
        return userStatusRepository.findAll();
    }

    @Override
    public void updateUserStatus(UUID id, Instant lastActiveAt) {
        UserStatus userStatus = userStatusRepository.findById(id);
        if (userStatus != null) {
            userStatus.update(lastActiveAt);
            userStatusRepository.save(userStatus);
        }
    }

    @Override
    public void deleteUserStatus(UUID id) {
        userStatusRepository.deleteById(id);
    }
}
