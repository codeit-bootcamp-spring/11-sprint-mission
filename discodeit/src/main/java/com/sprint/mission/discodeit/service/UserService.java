package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.controller.dto.UserUpdateApiRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.dto.user.CreateUserRequest;
import com.sprint.mission.discodeit.service.dto.user.UpdateUserRequest;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import com.sprint.mission.discodeit.service.dto.user.UserProfileRequest;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Transactional
    public UserDto create(CreateUserRequest request) {
        validateCreateRequest(request);
        validateUniqueUsername(request.username());
        validateUniqueEmail(request.email());

        // 프로필 BinaryContent는 User의 cascade 설정으로 함께 저장됨
        BinaryContent profile = toBinaryContentFromProfile(request.profile());
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .profile(profile)
                .build();

        // UserStatus는 User의 cascade 설정으로 함께 저장됨
        UserStatus userStatus = new UserStatus(user);
        user.assignStatus(userStatus);

        User savedUser = userRepository.save(user);
        log.info("사용자 생성 완료: id={}, username={}", savedUser.getId(), savedUser.getUsername());
        return toDto(savedUser);
    }

    @Transactional
    public UserDto create(CreateUserRequest request, MultipartFile profile) {
        CreateUserRequest mergedRequest = new CreateUserRequest(
                request.username(),
                request.email(),
                request.password(),
                toUserProfileRequest(profile)
        );
        return create(mergedRequest);
    }

    public UserDto find(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toDto(user);
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserDto update(UpdateUserRequest request) {
        if (request == null || request.userId() == null) {
            throw new DiscodeitException(ErrorCode.USER_ID_REQUIRED);
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        String updatedUsername = resolveUpdatedUsername(user, request);
        String updatedEmail = resolveUpdatedEmail(user, request);
        String updatedPassword = resolveUpdatedPassword(user, request);

        user.update(updatedUsername, updatedEmail, updatedPassword);
        replaceProfileIfPresent(user, request.replacementProfile());

        log.info("사용자 수정 완료: id={}", user.getId());
        return toDto(user);
    }

    @Transactional
    public UserDto update(UUID userId, UpdateUserRequest request, MultipartFile profile) {
        UpdateUserRequest mergedRequest = new UpdateUserRequest(
                userId,
                request.username(),
                request.email(),
                request.password(),
                toUserProfileRequest(profile)
        );
        return update(mergedRequest);
    }

    @Transactional
    public UserDto update(UUID userId, UserUpdateApiRequest request, MultipartFile profile) {
        return update(userId, new UpdateUserRequest(
                userId,
                request.newUsername(),
                request.newEmail(),
                request.newPassword(),
                null
        ), profile);
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        // User의 cascade 설정으로 profile, userStatus가 함께 삭제됨
        userRepository.delete(user);
        log.info("사용자 삭제 완료: id={}", id);
    }

    private void validateCreateRequest(CreateUserRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "요청값이 비어있어요.");
        }
        if (isBlank(request.username())) {
            throw new DiscodeitException(ErrorCode.USERNAME_REQUIRED);
        }
        if (isBlank(request.email())) {
            throw new DiscodeitException(ErrorCode.EMAIL_REQUIRED);
        }
        if (request.password() == null) {
            throw new DiscodeitException(ErrorCode.PASSWORD_REQUIRED);
        }
    }

    private BinaryContent toBinaryContentFromProfile(UserProfileRequest profile) {
        if (profile == null) {
            return null;
        }
        BinaryContent binaryContent = new BinaryContent(
                profile.data(),
                profile.fileName(),
                profile.contentType()
        );
        // User cascade 저장 전에 bytes를 스토리지에 저장 (ID는 생성자에서 이미 할당됨)
        binaryContentStorage.put(binaryContent.getId(), profile.data());
        return binaryContent;
    }

    private void replaceProfileIfPresent(User user, UserProfileRequest profile) {
        if (profile == null) {
            return;
        }
        BinaryContent newProfile = toBinaryContentFromProfile(profile);
        user.replaceProfile(newProfile);
    }

    private void validateUniqueUsername(UUID userId, String username) {
        if (isBlank(username)) {
            throw new DiscodeitException(ErrorCode.USERNAME_REQUIRED);
        }
        userRepository.findByUsername(username)
                .filter(foundUser -> !foundUser.getId().equals(userId))
                .ifPresent(user -> {
                    throw new DuplicateUsernameException(username);
                });
    }

    private void validateUniqueUsername(String username) {
        if (isBlank(username)) {
            throw new DiscodeitException(ErrorCode.USERNAME_REQUIRED);
        }
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
    }

    private void validateUniqueEmail(String email) {
        if (isBlank(email)) {
            throw new DiscodeitException(ErrorCode.EMAIL_REQUIRED);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    private void validateUniqueEmail(UUID userId, String email) {
        if (isBlank(email)) {
            throw new DiscodeitException(ErrorCode.EMAIL_REQUIRED);
        }
        userRepository.findByEmail(email)
                .filter(foundUser -> !foundUser.getId().equals(userId))
                .ifPresent(user -> {
                    throw new DuplicateEmailException(email);
                });
    }

    private String resolveUpdatedUsername(User user, UpdateUserRequest request) {
        if (request.username() == null) {
            return user.getUsername();
        }
        validateUniqueUsername(request.userId(), request.username());
        return request.username();
    }

    private String resolveUpdatedEmail(User user, UpdateUserRequest request) {
        if (request.email() == null) {
            return user.getEmail();
        }
        validateUniqueEmail(request.userId(), request.email());
        return request.email();
    }

    private String resolveUpdatedPassword(User user, UpdateUserRequest request) {
        if (isBlank(request.password())) {
            return user.getPassword();
        }
        return request.password();
    }

    private UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private UserProfileRequest toUserProfileRequest(MultipartFile profile) {
        if (profile == null || profile.isEmpty()) {
            return null;
        }
        try {
            return new UserProfileRequest(
                    profile.getBytes(),
                    profile.getOriginalFilename(),
                    profile.getContentType()
            );
        } catch (IOException exception) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "프로필 파일을 읽을 수 없어요.");
        }
    }
}
