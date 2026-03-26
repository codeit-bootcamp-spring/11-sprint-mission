package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusResponse create(UserStatusCreateRequest request){
        // User 검증
        if (userRepository.findById(request.userId()) == null){
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }

        // 중복 생성 방지
        if (userStatusRepository.findByUserId(request.userId()) != null){
            throw new IllegalStateException("이미 해당 유저의 UserStatus가 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(request.userId(), request.lastSeenAt());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return UserStatusResponse.of(savedUserStatus);
    }

    @Override
    public UserStatusResponse findById(UUID id){
        UserStatus userStatus = userStatusRepository.findById(id);

        if (userStatus == null){
            throw new IllegalStateException("존재하지 않는 UserStatus입니다.");
        }
        return UserStatusResponse.of(userStatus);
    }

    @Override
    public List<UserStatusResponse> findAll(){
        return userStatusRepository.findAll().stream()
                .map(UserStatusResponse::of)
                .toList();
    }

    @Override
    public UserStatusResponse update(UUID id, UserStatusUpdateRequest request){
        UserStatus userStatus = userStatusRepository.findById(id);

        if (userStatus == null){
            throw new IllegalStateException("존재하지 않는 UserStatus입니다.");
        }

        userStatus.updateLastSeenAt(request.lastSeenAt());
        UserStatus updatedUserStatus = userStatusRepository.save(userStatus);

        return UserStatusResponse.of(updatedUserStatus);
    }

    @Override
    public UserStatusResponse updateByUserId(UUID id, UserStatusUpdateRequest request){

        if (userStatusRepository.findById(id) == null){
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }

        UserStatus userStatus = userStatusRepository.findById(id);

        if (userStatus == null){
            throw new IllegalStateException("해당 유저의 UserStatus가 존재하지 않습니다.");
        }

        userStatus.updateLastSeenAt(request.lastSeenAt());
        UserStatus updatedUserStatus = userStatusRepository.save(userStatus);

        return UserStatusResponse.of(updatedUserStatus);
    }

    @Override
    public void deleteById(UUID id){
        if (userStatusRepository.findById(id) == null){
            throw new IllegalStateException("존재하지 않는 User입니다.");
        }

        userStatusRepository.delete(id);
    }
}
