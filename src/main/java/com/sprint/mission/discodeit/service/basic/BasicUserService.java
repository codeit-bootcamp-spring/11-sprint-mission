package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserDto create(UserCreateRequest request) {
        validateDuplicateUserName(request.userName());
        validateDuplicateEmail(request.email());

        User user = new User(
                request.userName(),
                request.email(),
                request.password(),
                request.statusMessage()
        );

        if (request.profileImage() != null) {
            BinaryContent savedProfile = saveBinaryContent(request.profileImage());
            user.update(
                    null,
                    null,
                    null,
                    null,
                    savedProfile.getId()
            );
        }

        User savedUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(savedUser.getId(), Instant.now());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return toDto(savedUser, savedUserStatus);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId());
        return toDto(user, userStatus);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> toDto(user, userStatusRepository.findByUserId(user.getId())))
                .toList();
    }

    @Override
    public UserDto update(UserUpdateRequest request) {
        User user = userRepository.findById(request.id());

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        if (request.userName() != null && !request.userName().equals(user.getUserName())) {
            validateDuplicateUserName(request.userName());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            validateDuplicateEmail(request.email());
        }

        UUID profileId = user.getProfileId();

        if (request.profileImage() != null) {
            if (user.getProfileId() != null) {
                binaryContentRepository.delete(user.getProfileId());
            }

            BinaryContent savedProfile = saveBinaryContent(request.profileImage());
            profileId = savedProfile.getId();
        }

        user.update(
                request.userName(),
                request.email(),
                request.password(),
                request.statusMessage(),
                profileId
        );

        User updatedUser = userRepository.save(user);
        UserStatus userStatus = userStatusRepository.findByUserId(updatedUser.getId());

        return toDto(updatedUser, userStatus);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }

        UserStatus userStatus = userStatusRepository.findByUserId(id);

        if (userStatus != null) {
            userStatusRepository.delete(userStatus.getId());
        }

        userRepository.delete(id);
    }

    private void validateDuplicateUserName(String userName) {
        if (userRepository.findByUserName(userName) != null) {
            throw new IllegalArgumentException("이미 사용중인 유저 이름입니다.");
        }
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    }

    private BinaryContent saveBinaryContent(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.contentType(),
                request.bytes()
        );
        return binaryContentRepository.save(binaryContent);
    }

    private UserDto toDto(User user, UserStatus userStatus) {
        boolean online = userStatus != null && userStatus.isOnline();

        return new UserDto(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getStatusMessage(),
                user.getProfileId(),
                online,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}