package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
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
    public UserDto.Response create(UserDto.CreateRequest request, BinaryContentDto.CreateRequest profileImageRequest) {
        // username 중복 확인
        if (userRepository.existsByName(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NAME);
        }

        // email 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        UUID profileImageId = null;
        if (profileImageRequest != null) {
            BinaryContent binaryContent = profileImageRequest.toEntity();
            BinaryContent savedContent = binaryContentRepository.save(binaryContent);
            profileImageId = savedContent.getId();
        }

        User user = request.toEntity(profileImageId);
        userRepository.save(user);

        // UserStatus 함께 생성
        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return UserDto.Response.of(user, userStatus);
    }

    @Override
    public UserDto.Response findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        return UserDto.Response.of(user, userStatus);
    }

    @Override
    public List<UserDto.Response> findAll() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    UserStatus status = userStatusRepository.findByUserId(user.getId())
                            .orElseGet(() -> UserStatus.builder()
                                    .userId(user.getId())
                                    .lastActiveAt(user.getCreatedAt())
                                    .build());

                    return UserDto.Response.of(user, status);
                })
                .toList();
    }

    @Override
    public UserDto.Response update(UUID id, UserDto.UpdateRequest request, BinaryContentDto.CreateRequest profileImageRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 프로필 이미지 변경 로직
        if (profileImageRequest != null) {
            // 기존 프로필 이미지 삭제
            if (user.getProfileImageId() != null) {
                binaryContentRepository.deleteById(user.getProfileImageId());
            }

            BinaryContent newImage = profileImageRequest.toEntity();
            user.updateProfileImage(binaryContentRepository.save(newImage).getId());
        }

        user.update(
                request.username(),
                request.nickname(),
                request.description(),
                request.email(),
                request.password()
        );

        userRepository.save(user);

        // 상태 정보 반환
        UserStatus status = userStatusRepository.findById(id)
                .orElseGet(() -> new UserStatus(id));

        return UserDto.Response.of(user, status);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));


        userStatusRepository.deleteById(id);

        // 프로필 이미지 삭제 (존재 시)
        if (user.getProfileImageId() != null) {
            binaryContentRepository.deleteById(user.getProfileImageId());
        }
        userRepository.deleteById(id);
    }
}