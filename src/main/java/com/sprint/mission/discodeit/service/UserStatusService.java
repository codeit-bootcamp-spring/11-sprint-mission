package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusUpdateDto;
import com.sprint.mission.discodeit.dto.UserStatusCreateDto;
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
public class UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    public UserStatus create(UserStatusCreateDto dto){
        // 없어도 될 듯
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        if (userStatusRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 UserStatus 입니다.");
        }
        UserStatus userStatus = new UserStatus(user.getId());
        return userStatusRepository.save(userStatus);
    }

    public UserStatus findByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        return userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));
    }

    public List<UserStatus> findAll() {
        return userStatusRepository.findAll();
    }

    public UserStatus update(UserStatusUpdateDto dto) {
        // 없어도 될 듯
        UserStatus userStatus = userStatusRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        userStatus.updateTime();

        return userStatusRepository.save(userStatus);
    }

    public UserStatus updateByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));

        userStatus.updateTime();

        return userStatusRepository.save(userStatus);
    }

    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        userStatusRepository.delete(userStatus.getId());
    }
}
