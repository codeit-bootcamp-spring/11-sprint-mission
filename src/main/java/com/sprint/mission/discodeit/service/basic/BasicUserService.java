package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserDto create(UserCreateRequest request) {
        if (userRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User(request.name(), request.email(), request.password());

        if (request.profileImageData() != null) {
            BinaryContent profileImage = new BinaryContent(request.profileImageData(),
                    "profile_image.jpg", (long) request.profileImageData().length,
                    "image/jpeg");
            binaryContentRepository.save(profileImage);
            user.updateProfile(profileImage.getId());
        }

        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return toDto(user, userStatus);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        UserStatus status = userStatusRepository.findByUserId(id).orElse(null);
        return toDto(user, status);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> toDto(user, userStatusRepository.findByUserId(user.getId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @Override
    public void update(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        user.update(request.name());

        if (request.profileImageData() != null) {
            if (user.getProfileId() != null) {
                binaryContentRepository.delete(user.getProfileId());
            }
            BinaryContent newProfile = new BinaryContent(request.profileImageData(),
                    "profile_image.jpg", (long) request.profileImageData().length,
                    "image/jpeg");
            binaryContentRepository.save(newProfile);
            user.updateProfile(newProfile.getId());
        }
        userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        userStatusRepository.deleteByUserId(user.getId());
        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }
        userRepository.delete(id);
    }

    @Override
    public UserDto login(UserLoginRequest request) {
        User user = userRepository.findAll().stream().filter(
                u -> u.getEmail().equals(request.email())).
                findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "가입되지 않은 이메일입니다."));
        if (!user.getPassword().equals(request.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        UserStatus status = userStatusRepository.findByUserId(
                user.getId()).orElse(null);
        return toDto(user, status);
    }

    private UserDto toDto(User user, UserStatus status) {
        boolean isOnline = false;

        if (status != null &&status.getUpdatedAt() != null) {
            isOnline = Duration.between(
                    status.getUpdatedAt(), Instant.now()).toMinutes() < 5;
        }
        return new UserDto(
                user.getId(), user.getCreatedAt(), user.getUpdatedAt(),
                user.getName(), user.getEmail(), user.getProfileId(), isOnline
        );
    }
}