package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserStatusService implements UserStatusService {
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusResponse createUserStatus(UserStatusCreateRequest userStatusCreateRequest) {
        User user = this.userRepository.findById(userStatusCreateRequest.userId());

        if (this.userStatusRepository.existByUserId(user.getId())) throw new IllegalArgumentException("user status has same user id already exists. ❌");

        UserStatus userStatus = new UserStatus(user);
        this.userStatusRepository.save(userStatus);

        log.info("user status has been created successfully. ✅ [ID: {}]", userStatus.getId());
        log.info("-> {user: {}}", user.getId());
        return userStatus.toResponse();
    }

    @Override
    public UserStatusResponse findById(UUID id) {
        UserStatus userStatus = this.userStatusRepository.findById(id);
        return userStatus.toResponse();
    }

    @Override
    public List<UserStatusResponse> findAll() {
        return this.userStatusRepository.findAll().stream()
                .map(UserStatus::toResponse)
                .toList();
    }

    @Override
    public UserStatusResponse updateUserStatus(UserStatusUpdateRequest userStatusUpdateRequest) {
        UserStatus userStatus = this.userStatusRepository.findById(userStatusUpdateRequest.id());

        userStatus.setUpdatedAt();
        this.userStatusRepository.save(userStatus);

        log.info("UserStatus has been updated successfully. ✅ [ID: {}]", userStatus.getId());
        return userStatus.toResponse();
    }

    @Override
    public UserStatusResponse updateUserStatusByUserId(UUID userId) {
        UserStatus userStatus = this.userStatusRepository.findByUserId(userId);

        userStatus.setUpdatedAt();
        this.userStatusRepository.save(userStatus);

        log.info("UserStatus has been updated successfully. ✅ [ID: {}]", userStatus.getId());
        log.info("-> {user: {}}", userId);
        return userStatus.toResponse();
    }

    @Override
    public void deleteUserStatus(UUID id) {
        UserStatus userStatus = this.userStatusRepository.findById(id);

        this.userStatusRepository.delete(userStatus);

        log.info("UserStatus has been deleted successfully. ✅ [ID: {}]", userStatus.getId());
    }
}
