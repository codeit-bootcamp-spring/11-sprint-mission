package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.UserStatusNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public UserStatus create(UserStatusCreateRequest request){
        if(userRepository.read(request.getUserId()) == null){
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if(userStatusRepository.readByUserId(request.getUserId()) != null){
            throw new IllegalArgumentException("이미 존재하는 UserStatus입니다.");
        }
        UserStatus userStatus = new UserStatus(request.getUserId(), Instant.now());
        return userStatusRepository.create(userStatus);
    }

    @Override
    public UserStatus read(UUID id){
        UserStatus userStatus = userStatusRepository.readByUserId(id);
        if(userStatus ==null){
            throw new UserStatusNotFoundException(id);
        }
        return userStatus;
    }

    @Override
    public List<UserStatus> readAll(){
        return userStatusRepository.readAll();
    }

    @Override
    public void update(UserStatusUpdateRequest request){
        UserStatus userStatus = userStatusRepository.readByUserId(request.getUserId());
        if(userStatus ==null){
            throw new UserStatusNotFoundException(request.getUserId());
        }
        userStatus.updateLastOnlineAt(request.getLastOnlineAt());
        userStatusRepository.update(request.getUserId(), request.getLastOnlineAt());
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.delete(id);
    }
}
