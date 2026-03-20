package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponseDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
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

    private final UserStatusRepository userStatusRepo;
    private final UserRepository userRepo;

    @Override
    public UserStatusResponseDto create(UserStatusCreateRequestDto dto) {
        userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        boolean exists = userStatusRepo.findAll().stream()
                .anyMatch(p -> p.getUserId().equals(dto.userId()));
        if(exists) throw new UserStatusAlreadyExistsException(dto.userId());

        UserStatus userStatus = new UserStatus(dto.userId());
        userStatusRepo.save(userStatus);
        return toDto(userStatus);
    }

    private UserStatusResponseDto toDto(UserStatus userStatus) {
        return new UserStatusResponseDto(
                userStatus.getId(), userStatus.getUserId(), userStatus.getUpdatedAt());
    }

    @Override
    public UserStatusResponseDto find(UUID id) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new UserStatusNotFoundException(id));

        return toDto(userStatus);
    }

    @Override
    public List<UserStatusResponseDto> findAll() {
        return userStatusRepo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void update(UUID id) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new UserStatusNotFoundException(id));

        userStatus.update();
        userStatusRepo.save(userStatus);
    }

    @Override
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepo.findById(id)
                .orElseThrow(() -> new UserStatusNotFoundException(id));

        userStatusRepo.delete(userStatus);
    }
}
