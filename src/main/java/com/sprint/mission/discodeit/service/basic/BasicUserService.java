package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService{

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;


    @Override
    public User create(UserCreateRequest dto) {
        boolean isDuplicated = userRepository.findAll().values().stream()
                .anyMatch(u -> u.getName().equals(dto.name()) || u.getEmail().equals(dto.email()));
        if (isDuplicated) {
            throw new IllegalArgumentException("이미 가입된 유저입니다.");
        }

        if (dto.profileImageId() != null && binaryContentRepository.findById(dto.profileImageId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 프로필 이미지입니다.");
        }

        User user = new User(dto.name(), dto.email(), dto.password(), dto.profileImageId());
        User savedUser = userRepository.save(user);

        UserStatus status = new UserStatus(savedUser.getId());
        userStatusRepository.save(status);

        return savedUser;
    }

    @Override
    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll().values().stream().toList();

        return users.stream()
                .map(u -> {
                    UserStatus status = userStatusRepository.findByUserId(u.getId())
                            .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));
                    return new UserResponse(u.getId(), u.getCreatedAt(), u.getUpdatedAt(), u.getName(), u.getEmail(), u.getProfileImageId(), status.isOnline());
                })
                .toList();
    }

    @Override
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        return new UserResponse(user.getId(), user.getCreatedAt(), user.getUpdatedAt(), user.getName(), user.getEmail(), user.getProfileImageId(), status.isOnline());
    }

    @Override
    public User update(UUID userId, UserUpdateRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));

        boolean isDuplicated = userRepository.findAll().values().stream()
                // 새로 받은 정보가 나를 제외하고, 다른 객체의 이름 및 이메일과 다른지 검시
                .filter(u -> !u.getId().equals(user.getId()))
                .anyMatch(u -> u.getName().equals(dto.name())
                        || u.getEmail().equals(dto.email()));
        if (isDuplicated) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임/이메일입니다.");
        }

        UUID userProfileImageId = user.getProfileImageId();
        // dto로 들어온 프로필 이미지가 null이 아니고, 내 기존 프로필 이미지랑 다를 때만 검사 및 삭제
        if (dto.profileImageId() != null && !dto.profileImageId().equals(userProfileImageId)) {
            if (binaryContentRepository.findById(dto.profileImageId()).isEmpty()) {
                throw new IllegalArgumentException("존재하지 않는 프로필 이미지입니다.");
            }
            if (userProfileImageId != null) {
                binaryContentRepository.delete(userProfileImageId);
            }
            userProfileImageId = dto.profileImageId();
        }

        user.update(dto.name(), dto.email(), dto.password(), userProfileImageId);

        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));

        if (user.getProfileImageId() != null) {
            binaryContentRepository.delete(user.getProfileImageId());
        }

        userStatusRepository.deleteByUserId(user.getId());
        userRepository.delete(user.getId());
    }
}
