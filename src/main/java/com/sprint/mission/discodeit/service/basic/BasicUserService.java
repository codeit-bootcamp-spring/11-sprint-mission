package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.user.CreateUserRequest;
import com.sprint.mission.discodeit.dto.request.user.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
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
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        // username, email 중복 체크
        boolean duplicated = userRepository.findAll().stream()
                .anyMatch(u -> u.getUsername().equals(request.getUsername())
                        || u.getEmail().equals(request.getEmail()));
        if (duplicated) {
            throw new IllegalArgumentException("이미 사용 중인 username 또는 email입니다.");
        }


        UUID profileId = null;
        if (request.getProfile() != null) {
            BinaryContent profile = new BinaryContent(
                    request.getProfile().getFileName(),
                    request.getProfile().getSize(),
                    request.getProfile().getContentType(),
                    request.getProfile().getBytes()
            );
            binaryContentRepository.save(profile);
            profileId = profile.getId();
        }

        // 유저 생성
        User user = new User(request.getUsername(), request.getEmail(), request.getPassword(), profileId);
        userRepository.save(user);

        // UserStatus 생성
        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return toResponse(user, userStatus);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));
        UserStatus userStatus = userStatusRepository.findByUserId(id).orElse(null);
        return toResponse(user, userStatus);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
                    return toResponse(user, userStatus);
                })
                .toList();
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        // 기존 프로필 삭제 후 새 프로필 저장 (선택적)
        if (request.getProfile() != null) {
            // 기존 프로필 삭제
            if (user.getProfileId() != null) {
                binaryContentRepository.deleteById(user.getProfileId());
            }
            // 새 프로필 저장
            BinaryContent profile = new BinaryContent(
                    request.getProfile().getFileName(),
                    request.getProfile().getSize(),
                    request.getProfile().getContentType(),
                    request.getProfile().getBytes()
            );
            binaryContentRepository.save(profile);
            user.updateProfile(profile.getId());
        }

        user.update(request.getUsername(), request.getEmail(), request.getPassword());
        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findByUserId(id).orElse(null);
        return toResponse(user, userStatus);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User입니다."));

        if (user.getProfileId() != null) {
            binaryContentRepository.deleteById(user.getProfileId());
        }
        UserStatus userStatus = userStatusRepository.findByUserId(id).orElse(null);
        if (userStatus != null) {
            userStatusRepository.deleteById(userStatus.getId());
        }

        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user, UserStatus userStatus) {
        boolean isOnline = userStatus != null && userStatus.isOnline();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                isOnline,
                user.getProfileId(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
