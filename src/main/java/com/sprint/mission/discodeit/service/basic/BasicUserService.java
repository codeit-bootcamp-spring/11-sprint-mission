package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {
        if (request.getUserName() == null || request.getUserName().isBlank()) {
            throw new IllegalArgumentException("유저명이 null이거나 blank입니다.");
        }
        if (request.getUserEmail() == null || request.getUserEmail().isBlank()) {
            throw new IllegalArgumentException("email이 null이거나 blank입니다.");
        }

        boolean isDuplicateName = userRepository.readAll().stream()
                .anyMatch(u -> u.getUserName().equals(request.getUserName()));
        boolean isDuplicateEmail = userRepository.readAll().stream()
                .anyMatch(u -> u.getUserEmail().equals(request.getUserEmail()));
        if (isDuplicateName) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }
        if (isDuplicateEmail) {
            throw new IllegalArgumentException("이미 존재하는 email입니다.");
        }

        User user = new User(request.getUserName(), request.getUserEmail(), request.getUserPassword());
        userRepository.create(user);

        if (request.getFileName() != null) {
            BinaryContent binaryContent = BinaryContent.forProfile(user.getId(), request.getFileName(), request.getFileContent(),request.getContentType());
            binaryContentRepository.create(binaryContent);
        }

        UserStatus userStatus = new UserStatus(user.getId(), Instant.now());
        userStatusRepository.create(userStatus);

        return new UserResponse(user.getId(), user.getUserName(), user.getUserEmail(), userStatus.isOnline());
    }

    @Override
    public UserResponse read(UUID id) {
        User user = userRepository.read(id);
        if(user == null) throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        UserStatus userStatus = userStatusRepository.readByUserId(id);
        return new UserResponse(user.getId(), user.getUserName(), user.getUserEmail(), userStatus.isOnline());
    }

    @Override
    public List<UserResponse> readAll() {
        return userRepository.readAll().stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.readByUserId(user.getId());
                    return new UserResponse(user.getId(), user.getUserName(), user.getUserEmail(), userStatus.isOnline());
                })
                .toList();

    }

    @Override
    public void update(UserUpdateRequest request) {
        User user = userRepository.read(request.getId());
        if (user == null) throw new IllegalArgumentException("존재하지 않는 유저입니다.");

        if (request.getUserName() == null || request.getUserName().isBlank()) {
            throw new IllegalArgumentException("유저명이 null이거나 blank입니다.");
        }
        if (request.getUserEmail() == null || request.getUserEmail().isBlank()) {
            throw new IllegalArgumentException("email이 null이거나 blank입니다.");
        }

        boolean isDuplicateName = userRepository.readAll().stream()
                .filter(u -> !u.getId().equals(request.getId()))
                .anyMatch(u -> u.getUserName().equals(request.getUserName()));
        if (isDuplicateName) throw new IllegalArgumentException("이미 존재하는 유저입니다.");

        boolean isDuplicateEmail = userRepository.readAll().stream()
                .filter(u -> !u.getId().equals(request.getId()))
                .anyMatch(u -> u.getUserEmail().equals(request.getUserEmail()));
        if (isDuplicateEmail) throw new IllegalArgumentException("이미 존재하는 email입니다.");

        if(request.getFileName() != null){
            binaryContentRepository.deleteByUserId(user.getId());
            BinaryContent binaryContent = BinaryContent.forProfile(user.getId(), request.getFileName(), request.getFileContent(),request.getContentType());
            binaryContentRepository.create(binaryContent);
        }

        user.updateUser(request.getUserName(), request.getUserEmail(), request.getUserPassword());
        userRepository.create(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.read(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        binaryContentRepository.deleteByUserId(id);
        userStatusRepository.delete(id);
        userRepository.delete(id);
    }
}