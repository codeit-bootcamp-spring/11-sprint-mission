package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepo;
    private final UserRepository userRepo;
    private final UserStatusMapper userStatusMapper;

    @Override
    @Transactional
    public UserStatusDto create(UserStatusCreateRequest dto) {
        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        if(userStatusRepo.findByUser(user).isPresent())
            throw new UserStatusAlreadyExistsException(user.getId());

        UserStatus userStatus = new UserStatus(user, dto.lastActiveAt());
        userStatusRepo.save(userStatus);

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto find(UUID id) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new UserStatusNotFoundException(id));

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto findByUserId(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserStatus userStatus = userStatusRepo.findByUser(user)
                .orElseThrow(() -> new UserStatusNotFoundException(user));

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public List<UserStatusDto> findAll() {
        return userStatusRepo.findAllWithUser().stream()
                .map(userStatusMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void update(UUID id, UserStatusUpdateRequest dto) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new UserStatusNotFoundException(id));

        userStatus.update(dto.newLastActiveAt());
    }

    @Override
    @Transactional
    public void updateByUserId(UUID id, UserStatusUpdateRequest dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserStatus userStatus = userStatusRepo.findByUser(user)
                .orElseThrow(() -> new UserStatusNotFoundException(user));

        userStatus.update(dto.newLastActiveAt());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new UserStatusNotFoundException(id));
        userStatusRepo.delete(userStatus);
    }
}
