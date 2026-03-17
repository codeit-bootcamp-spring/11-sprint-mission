package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
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
    private final UserService userService;

    @Override
    public UserStatus createUserStatus(UserStatusCreateDto statusCreateDto) {
        userRepository.findById(statusCreateDto.userId())
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        return userStatusRepository.findByUserId(statusCreateDto.userId())
                .map(existStatus -> {
                    System.out.println("상태 정보가 존재하여 기존 정보를 반환합니다.");
                    userStatusRepository.save(existStatus);
                    return existStatus;
                })
                .orElseGet(() -> {
                   UserStatus userStatus = new UserStatus(statusCreateDto.userId());
                   userStatusRepository.save(userStatus);
                    System.out.println("유저 상태 신규 생성완료");
                    userStatusRepository.save(userStatus);
                    return userStatus;
                });
    }

    @Override
    public UserStatus findUserStatus(UUID id) {
        return userStatusRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상태 기록이 존재하지 않습니다."));
    }

    @Override
    public List<UserStatus> findAllUserStatus() {
        List<UserStatus> allUserStatus = userStatusRepository.findAll();

        System.out.println("전체 유저 상태 조회완료");

        return allUserStatus;
    }

    @Override
    public void deleteUserStatus(UUID id) {
        UserStatus status = userStatusRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 유저 상태가 존재하지 않습니다."));

        userStatusRepository.deleteByUserId(id);

        System.out.println("✅ 유저 상태 삭제 완료");
    }

    @Override
    public UserStatus updateUserStatus(UserStatusUpdateDto request) {
        return userStatusRepository.findById(request.id())
                .map(status -> {
                    status.update();
                    userStatusRepository.save(status);
                    return status;
                })
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상태 기록이 존재하지 않습니다."));
    }

    @Override
    public UserStatus updateUserIdStatus(UserStatusUpdateDto request) {
        return userStatusRepository.findByUserId(request.userId())
                .map(status -> {
                    status.update();
                    userStatusRepository.save(status);
                    return status;
                })
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 상태 정보가 존재하지 않습니다."));
    }
}
