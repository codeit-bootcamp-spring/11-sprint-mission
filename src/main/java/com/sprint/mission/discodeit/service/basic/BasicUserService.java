package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepo;
    private final UserStatusRepository userStatusRepo;
    private final BinaryContentRepository binaryContentRepo;

    @Override
    public UserDto create(UserCreateRequest dto, MultipartFile profile) {
        if(userRepo.findByName(dto.username()).isPresent()) throw new BusinessException(ErrorCode.DUPLICATE_NAME);
        if(userRepo.findByEmail(dto.email()).isPresent()) throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);

        UUID profileId = saveProfile(profile);

        User user = new User(dto.username(), dto.email(), dto.password(), profileId);
        userRepo.save(user);

        UserStatus userStatus = new UserStatus(user.getId(), Instant.now());
        userStatusRepo.save(userStatus);

        return toDto(user, userStatus);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserStatus userStatus = userStatusRepo.findByUserId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));

        return toDto(user, userStatus);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepo.findAll().stream()
                .map(user -> {
                            UserStatus userStatus = userStatusRepo.findByUserId(user.getId())
                                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
                            return toDto(user, userStatus);
                        }
                )
                .toList();
    }

    @Override
    public void update(UUID id, UserUpdateRequest dto, MultipartFile profile) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(!user.getUsername().equals(dto.newUsername()) && userRepo.findByName(dto.newUsername()).isPresent())
            throw new BusinessException(ErrorCode.DUPLICATE_NAME);
        if(!user.getEmail().equals(dto.newEmail()) && userRepo.findByEmail(dto.newEmail()).isPresent())
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);

        UUID oldProfileId = user.getProfileId();
        UUID newProfileId = saveProfile(profile);

        user.setUsername(dto.newUsername());
        user.setEmail(dto.newEmail());
        user.setPassword(dto.newPassword());
        if(newProfileId != null) user.setProfileId(newProfileId);

        user.update();
        userRepo.save(user);

        if(newProfileId != null && oldProfileId != null) {
            binaryContentRepo.deleteById(oldProfileId);
        }
    }

    @Override
    public void delete(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserStatus userStatus = userStatusRepo.findByUserId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
        userStatusRepo.deleteById(userStatus.getId());

        UUID profileId = user.getProfileId();
        if(profileId != null) {
            binaryContentRepo.findById(profileId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND));
            binaryContentRepo.deleteById(profileId);
        }

        userRepo.deleteById(id);
    }

    private UUID saveProfile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            return null;
        }

        try {
            BinaryContent binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            binaryContentRepo.save(binaryContent);
            return binaryContent.getId();
        } catch (IOException e){
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    private UserDto toDto(User user, UserStatus userStatus) {
        return new UserDto(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.passed()
        );
    }
}
