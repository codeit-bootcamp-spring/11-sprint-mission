package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateParam;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserDto create(UserCreateRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 username입니다.");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 email입니다.");
        }

        UUID profileId = null;

        if (request.profileImage() != null) {
            BinaryContent profile = new BinaryContent(
                    request.profileImage().fileName(),
                    request.profileImage().contentType(),
                    request.profileImage().bytes()
            );

            BinaryContent savedProfile = binaryContentRepository.save(profile);
            profileId = savedProfile.getId();
        }

        User user = new User(
                request.username(),
                request.email(),
                request.password()
        );

        if (profileId != null) {
            user.updateProfile(profileId);
        }

        User savedUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(savedUser.getId(), Instant.now());
        userStatusRepository.save(userStatus);

        return toDto(savedUser);
    }

    @Override
    public Optional<UserDto> find(UUID id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();
        return Optional.of(toDto(user));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserDto update(UserUpdateParam param) {
        User user = userRepository.findById(param.id())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        String nextUsername = user.getUsername();
        if (param.request().newUsername() != null && !param.request().newUsername().isBlank()) {
            nextUsername = param.request().newUsername();
        }

        String nextEmail = user.getEmail();
        if (param.request().newEmail() != null && !param.request().newEmail().isBlank()) {
            nextEmail = param.request().newEmail();
        }

        String nextPassword = user.getPassword();
        if (param.request().newPassword() != null && !param.request().newPassword().isBlank()) {
            nextPassword = param.request().newPassword();
        }

        Optional<User> userByUsername = userRepository.findByUsername(nextUsername);
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 username입니다.");
        }

        Optional<User> userByEmail = userRepository.findByEmail(nextEmail);
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 email입니다.");
        }

        user.update(
                param.request().newUsername(),
                param.request().newEmail(),
                param.request().newPassword()
        );

        if (param.request().profileImage() != null) {
            if (user.getProfileId() != null) {
                binaryContentRepository.delete(user.getProfileId());
            }

            BinaryContent newProfile = new BinaryContent(
                    param.request().profileImage().fileName(),
                    param.request().profileImage().contentType(),
                    param.request().profileImage().bytes()
            );

            BinaryContent savedProfile = binaryContentRepository.save(newProfile);
            user.updateProfile(savedProfile.getId());
        }

        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }

        userStatusRepository.findByUserId(user.getId())
                .ifPresent(status -> userStatusRepository.delete(status.getId()));

        userRepository.delete(id);
    }

    private UserDto toDto(User user) {
        boolean online = userStatusRepository.findByUserId(user.getId())
                .map(UserStatus::isOnline)
                .orElse(false);

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getProfileId(),
                online,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}