package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
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
        User user = this.userRepository.findById(userStatusCreateRequest.userId())
                .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌"));

        if (this.userStatusRepository.existByUserId(user.getId())) throw new IllegalArgumentException("user status has same user id already exists. ❌");

        UserStatus userStatus = new UserStatus(user);
        this.userStatusRepository.save(userStatus);

        log.info("user status has been created successfully. ✅ [ID: {}]", userStatus.getId());
        log.info("-> {user: {}}", user.getId());
        return userStatus.toResponse();
    }

    @Override
    public UserStatusResponse findById(UUID id) {
        return this.userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"))
                .toResponse();
    }

    @Override
    public List<UserStatusResponse> findAll() {
        return this.userStatusRepository.findAll().stream()
                .map(UserStatus::toResponse)
                .toList();
    }

    @Override
    public UserStatusResponse updateUserStatus(UserStatusUpdateRequest userStatusUpdateRequest) {
        UserStatus userStatus = this.userStatusRepository.findById(userStatusUpdateRequest.id())
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));

        userStatus.setUpdatedAt();
        this.userStatusRepository.save(userStatus);

        log.info("UserStatus has been updated successfully. ✅ [ID: {}]", userStatus.getId());
        return userStatus.toResponse();
    }

    @Override
    public UserStatusResponse updateUserStatusByUserId(UUID userId) {
        UserStatus userStatus = this.userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));

        userStatus.setUpdatedAt();
        this.userStatusRepository.save(userStatus);

        log.info("UserStatus has been updated successfully. ✅ [ID: {}]", userStatus.getId());
        log.info("-> {user: {}}", userId);
        return userStatus.toResponse();
    }

    @Override
    public void deleteUserStatus(UUID id) {
        UserStatus userStatus = this.userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));

        this.userStatusRepository.delete(userStatus);

        log.info("UserStatus has been deleted successfully. ✅ [ID: {}]", userStatus.getId());
    }
}
