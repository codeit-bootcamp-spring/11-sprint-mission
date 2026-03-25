package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusDto.Response create(UserStatusDto.CreateRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NoSuchElementException("User not found with id: " + request.userId());
        }

        // 중복 생성 방지
        boolean isDuplicate = userStatusRepository.findAll().stream()
                .anyMatch(us -> us.getUserId().equals(request.userId()));
        if (isDuplicate) {
            throw new IllegalStateException("UserStatus already exists for user: " + request.userId());
        }

        UserStatus userStatus = request.toEntity();
        return UserStatusDto.Response.of(userStatusRepository.save(userStatus));
    }

    @Override
    public UserStatusDto.Response findById(UUID id) {
        return userStatusRepository.findById(id)
                .map(UserStatusDto.Response::of)
                .orElseThrow(() -> new NoSuchElementException("UserStatus not found with id: " + id));
    }

    @Override
    public List<UserStatusDto.Response> findAll() {
        return userStatusRepository.findAll().stream()
                .map(UserStatusDto.Response::of)
                .toList();
    }

    @Override
    public UserStatusDto.Response update(UUID id, UserStatusDto.UpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("UserStatus not found with id: " + id));

        userStatus.updateActiveTime();

        return UserStatusDto.Response.of(userStatusRepository.save(userStatus));
    }

    @Override
    public UserStatusDto.Response updateByUserId(UUID userId, UserStatusDto.UpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findAll().stream()
                .filter(us -> us.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("UserStatus not found for user: " + userId));

        userStatus.updateActiveTime();

        return UserStatusDto.Response.of(userStatusRepository.save(userStatus));
    }

    @Override
    public void delete(UUID id) {
        if (!userStatusRepository.existsById(id)) {
            throw new NoSuchElementException("UserStatus not found with id: " + id);
        }
        userStatusRepository.deleteById(id);
    }
}