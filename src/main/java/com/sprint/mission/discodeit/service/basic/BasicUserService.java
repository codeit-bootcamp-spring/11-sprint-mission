package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.CreateBinaryContentRequestDTO;
import com.sprint.mission.discodeit.dto.user.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final MessageRepository messageRepository;
    private final UserChannelRepository userChannelRepository;
    private final ReadStatusRepository readStatusRepository;

    private final BinaryContentService binaryContentService;

    @Override
    public List<UserDto> findAllUserDtos() {
        return userRepository.findAll().stream()
                .map(user -> {
                    // UserStatus 조회 로직
                    boolean isOnline = userStatusRepository.findByUserId(user.getId())
                            .map(status -> "ONLINE".equals(status.calculateCurrentStatus()))
                            .orElse(false);

                    return UserDto.from(user, isOnline);
                })
                .toList();
    }

    @Override
    public User createUser(UserCreateRequest request, MultipartFile profile) {
        // email & username 중복 확인
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_USERNAME_DUPLICATE);
        }

        // 프로필 이미지 저장
        UUID profileId = saveProfileImage(profile);

        // 새로운 유저 생성
        User newUser = User.create(
                request.username(),
                request.email(),
                request.password(),
                profileId
        );
        User savedUser = userRepository.save(newUser);

        // 새로운 유저 상태 정보 생성
        UserStatus userStatus = UserStatus.create(newUser.getId());
        userStatusRepository.save(userStatus);

        return savedUser;
    }

    @Override
    public User updateUser(UUID userId, UserUpdateRequest request, MultipartFile profile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.newEmail() != null && !request.newEmail().equals(user.getEmail()) && userRepository.findByEmail(request.newEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        if (request.newUsername() != null && !request.newUsername().equals(user.getUsername()) && userRepository.findByUsername(request.newUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_USERNAME_DUPLICATE);
        }

        if (request.newUsername() != null) user.updateUsername(request.newUsername());
        if (request.newEmail() != null) user.updateEmail(request.newEmail());
        if (request.newPassword() != null) user.updatePassword(request.newPassword());

        // 새로운 프로필 사진 업로드 처리
        if (profile != null && !profile.isEmpty()) {
            if (user.getProfileId() != null) {
                binaryContentService.delete(user.getProfileId());
            }
            UUID newProfileId = saveProfileImage(profile);
            user.updateProfileId(newProfileId);
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 프로필 사진 삭제
        if (user.getProfileId() != null) {
            binaryContentService.delete(user.getProfileId());
        }

        userStatusRepository.findByUserId(userId).ifPresent(status -> userStatusRepository.deleteById(status.getId()));
        userChannelRepository.findAllByUserId(userId).forEach(uc -> userChannelRepository.deleteById(uc.getId()));
        messageRepository.findAllByUserId(userId).forEach(m -> {
            m.getAttachmentIds().forEach(binaryContentService::delete);
            messageRepository.deleteById(m.getId());
        });
        readStatusRepository.findByUserId(userId).forEach(rs -> readStatusRepository.deleteById(rs.getId()));

        userRepository.deleteById(userId);
    }

    // 파일 저장
    private UUID saveProfileImage(MultipartFile profile) {
        if (profile == null || profile.isEmpty()) return null;
        try {
            CreateBinaryContentRequestDTO fileDto = new CreateBinaryContentRequestDTO(
                    profile.getOriginalFilename(),
                    profile.getContentType(),
                    profile.getBytes()
            );
            return binaryContentService.create(fileDto).getId();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_IO_ERROR);
        }
    }
}
