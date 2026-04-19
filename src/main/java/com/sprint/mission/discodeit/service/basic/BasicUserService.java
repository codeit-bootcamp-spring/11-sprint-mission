package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(
            UserCreateRequest userCreateRequest,
            Optional<BinaryContentCreateRequest> optionalProfileCreateRequest
    ) {
        String username = userCreateRequest.username();
        String email = userCreateRequest.email();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User with username " + username + " already exists");
        }

        BinaryContent nullableProfile = optionalProfileCreateRequest
                .map(profileRequest -> {
                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();

                    BinaryContent binaryContent = new BinaryContent(
                            fileName,
                            (long) bytes.length,
                            contentType
                    );

                    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
                    binaryContentStorage.put(savedBinaryContent.getId(), bytes);

                    return savedBinaryContent;
                })
                .orElse(null);

        User user = new User(
                userCreateRequest.username(),
                userCreateRequest.email(),
                userCreateRequest.password(),
                nullableProfile
        );
        User createdUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(createdUser, Instant.now());
        userStatusRepository.save(userStatus);

        return userMapper.toDto(createdUser, userStatus.isOnline());
    }

    @Override
    public UserDto find(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> userMapper.toDto(user,
                        user.getUserStatus() != null ? user.getUserStatus().isOnline() : null))
                .orElseThrow(() ->
                        new NoSuchElementException("User with id " + userId + " not found"));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> userMapper.toDto(user,
                        user.getUserStatus() != null ? user.getUserStatus().isOnline() : null))
                .toList();
    }

    @Override
    @Transactional
    public UserDto update(
            UUID userId,
            UserUpdateRequest userUpdateRequest,
            Optional<BinaryContentCreateRequest> optionalProfileCreateRequest
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NoSuchElementException("User with id " + userId + " not found"));

        String newUsername = userUpdateRequest.newUsername();
        String newEmail = userUpdateRequest.newEmail();

        if (newEmail != null
                && !newEmail.equals(user.getEmail())
                && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("User with email " + newEmail + " already exists");
        }

        if (newUsername != null
                && !newUsername.equals(user.getUsername())
                && userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("User with username " + newUsername + " already exists");
        }

        BinaryContent nullableProfile = optionalProfileCreateRequest
                .map(profileRequest -> {
                    if (user.getProfile() != null) {
                        binaryContentRepository.deleteById(user.getProfile().getId());
                    }

                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();

                    BinaryContent binaryContent = new BinaryContent(
                            fileName,
                            (long) bytes.length,
                            contentType
                    );

                    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
                    binaryContentStorage.put(savedBinaryContent.getId(), bytes);

                    return savedBinaryContent;
                })
                .orElse(user.getProfile());

        user.update(newUsername, newEmail, userUpdateRequest.newPassword(), nullableProfile);
        return userMapper.toDto(user,
                user.getUserStatus() != null ? user.getUserStatus().isOnline() : null);
    }

    @Override
    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NoSuchElementException("User with id " + userId + " not found"));

        if (user.getProfile() != null) {
            binaryContentRepository.deleteById(user.getProfile().getId());
        }

        userRepository.deleteById(userId);
    }
}