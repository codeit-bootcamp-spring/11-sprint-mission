package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements com.sprint.mission.discodeit.service.UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatus create(UserStatusCreateRequest dto){
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        if (userStatusRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 UserStatus 입니다.");
        }
        UserStatus userStatus = new UserStatus(user.getId());
        return userStatusRepository.save(userStatus);
    }

    @Override
    public UserStatus findByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        return userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));
    }

    @Override
    public List<UserStatus> findAll() {
        return userStatusRepository.findAll();
    }

    @Override
    public UserStatus update(UserStatusUpdateRequest dto) {
        UserStatus userStatus = userStatusRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        userStatus.updateTime();

        return userStatusRepository.save(userStatus);
    }

    @Override
    public UserStatus updateByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));

        userStatus.updateTime();

        return userStatusRepository.save(userStatus);
    }

    @Override
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        userStatusRepository.delete(userStatus.getId());
    }
}
