package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userStatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userStatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Profile("service-basic")
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void create(UserStatusCreateRequest request) {
        UUID userId = request.getUserId();

        if (userRepository.findById(userId) == null) {
            throw new UserNotFoundException("존재하지 않는 유저입니다. (userId: " + userId + ")");
        }

        boolean isExist = userStatusRepository.findAll().stream()
                .anyMatch(status -> status.getUserId().equals(userId));

        if (isExist) {
            throw new UserStatusAlreadyExistsException("해당 유저의 상태 정보가 이미 존재합니다. (userId: " + userId + ")");
        }

        UserStatus userStatus = new UserStatus(userId);
        userStatusRepository.save(userStatus);
    }

    @Override
    @Transactional
    public UserStatus read(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id);
        if (userStatus == null) {
            throw new UserStatusNotFoundException("조회할 유저 상태 정보를 찾을 수 없습니다. (ID: " + id + ")");
        }
        return userStatus;
    }

    @Override
    @Transactional
    public List<UserStatus> readAll() {
        return userStatusRepository.findAll();
    }

    @Override
    @Transactional
    public void update(UUID id, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(id);

        if (userStatus == null) {
            throw new UserStatusNotFoundException("수정할 유저 상태 정보를 찾을 수 없습니다. (ID: " + id + ")");
        }

        userStatus.updateTime();
        userStatusRepository.save(userStatus);
    }

    @Override
    @Transactional
    public void updateByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findAll().stream()
                .filter(status -> status.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserStatusNotFoundException("해당 유저의 상태 정보를 찾을 수 없습니다. (userId: " + userId + ")"));

        userStatus.updateTime();
        userStatusRepository.save(userStatus);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (userStatusRepository.findById(id) == null) {
            throw new UserStatusNotFoundException("삭제할 유저 상태 정보를 찾을 수 없습니다. (ID: " + id + ")");
        }
        userStatusRepository.deleteById(id);
    }
}