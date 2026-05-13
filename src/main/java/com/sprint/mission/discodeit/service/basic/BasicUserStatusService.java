package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateParam;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DuplicatedException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;

    @Override
    @Transactional
    public UserStatusDto create(UserStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        boolean alreadyExists = userStatusRepository.existsByUser_Id(request.userId());

        if (alreadyExists) {
            throw new DuplicatedException("이미 해당 사용자의 UserStatus가 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(user, request.lastActiveAt());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(savedUserStatus);
    }

    @Override
    public Optional<UserStatusDto> find(UUID id) {
        return userStatusRepository.findById(id).map(userStatusMapper::toDto);
    }

    @Override
    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll().stream()
                .map(userStatusMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserStatusDto update(UserStatusUpdateParam param) {
        UserStatus userStatus = userStatusRepository.findById(param.id())
                .orElseThrow(() -> new NotFoundException("해당 UserStatus가 존재하지 않습니다."));

        userStatus.update(param.request().newLastActiveAt());
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자의 UserStatus가 존재하지 않습니다."));

        userStatus.update(request.newLastActiveAt());
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        userStatusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 UserStatus가 존재하지 않습니다."));

        userStatusRepository.deleteById(id);
    }
}