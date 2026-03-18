package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.CreateUserStatusRequestDTO;
import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusRequestDTO;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserStatus create(
            CreateUserStatusRequestDTO dto
    ) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("해당 유저는 없습니다."));

        if (userStatusRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("해당 유저의 UserStatus는 이미 존재합니다.");
        }

        UserStatus newUserStatus = UserStatus.create(user.getId());

        return userStatusRepository.save(newUserStatus);
    }

    @Override
    public UserStatus find(
            UUID userStatusId
    ) {
        return  userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new RuntimeException("해당 UserStatus는 없습니다."));
    }

    public List<UserStatus> findAll() {
        return userStatusRepository.findAll();
    }

    @Override
    public UserStatus update(
            UpdateUserStatusRequestDTO dto
    ) {
        UserStatus userStatus = find(dto.userStatusId());

        userStatus.updateLastOnlineTime();

        return userStatusRepository.save(userStatus);
    }

    @Override
    public UserStatus updateByUserId(
            UUID userId
    ) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 UserStatus는 없습니다."));

        userStatus.updateLastOnlineTime();

        return userStatusRepository.save(userStatus);
    }

    @Override
    public void delete(
            UUID userStatusId
    ) {
        UserStatus userStatus = find(userStatusId);

        userStatusRepository.deleteById(userStatus.getId());
    }
}
