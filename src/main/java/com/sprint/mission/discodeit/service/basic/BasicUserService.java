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

        // 프로필 이미지 저장 (선택적)
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
        User user = userRepository.findById(id);
        UserStatus userStatus = userStatusRepository.findByUserId(id);
        return toResponse(user, userStatus);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId());
                    return toResponse(user, userStatus);
                })
                .toList();
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id);

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

        UserStatus userStatus = userStatusRepository.findByUserId(id);
        return toResponse(user, userStatus);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id);

        // 프로필 이미지 삭제
        if (user.getProfileId() != null) {
            binaryContentRepository.deleteById(user.getProfileId());
        }
        // UserStatus 삭제
        UserStatus userStatus = userStatusRepository.findByUserId(id);
        if (userStatus != null) {
            userStatusRepository.deleteById(userStatus.getId());
        }

        userRepository.deleteById(id);
    }

    // Entity → Response DTO 변환 메서드
    private UserResponse toResponse(User user, UserStatus userStatus) {
        boolean isOnline = userStatus != null && userStatus.isOnline();
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), isOnline, user.getProfileId());
    }
}
