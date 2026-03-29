package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final ReadStatusRepository readStatusRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^\\d{3}-\\d{3,4}-\\d{4}$";

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest, Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
        if (userCreateRequest.nickname() == null || userCreateRequest.nickname().isBlank())
            throw new ApiException(USER_NICKNAME_REQUIRED);

        if (userCreateRequest.username() == null || userCreateRequest.username().isBlank())
            throw new ApiException(USER_USERNAME_REQUIRED);
        if (this.userRepository.existByUsername(userCreateRequest.username()))
            throw new ApiException(USER_USERNAME_DUPLICATED);

        if (userCreateRequest.email() == null || userCreateRequest.email().isBlank())
            throw new ApiException(USER_EMAIL_REQUIRED);
        if (!userCreateRequest.email().matches(EMAIL_REGEX))
            throw new ApiException(USER_INVALID_EMAIL_FORMAT);
        if (this.userRepository.existByEmail(userCreateRequest.email()))
            throw new ApiException(USER_EMAIL_DUPLICATED);

        if (userCreateRequest.password() == null || userCreateRequest.password().isBlank())
            throw new ApiException(USER_PASSWORD_REQUIRED);
        if (userCreateRequest.password().length() < 8)
            throw new ApiException(USER_INVALID_PASSWORD_LENGTH);

        if (userCreateRequest.phoneNumber() == null || userCreateRequest.phoneNumber().isBlank())
            throw new ApiException(USER_PHONE_NUMBER_REQUIRED);
        if (!userCreateRequest.phoneNumber().matches(PHONE_REGEX))
            throw new ApiException(USER_INVALID_PHONE_NUMBER_FORMAT);

        BinaryContent profile = null;
        if (binaryContentCreateRequest.isPresent()) {
            BinaryContentCreateRequest req = binaryContentCreateRequest.get();
            profile = new BinaryContent(req.data(), req.fileName(), req.contentType(), req.size());
            this.binaryContentRepository.save(profile);
        }

        User user = new User(
                userCreateRequest.nickname(),
                userCreateRequest.username(),
                userCreateRequest.email(),
                userCreateRequest.password(),
                userCreateRequest.phoneNumber(),
                profile != null ? profile.getId() : null
        );
        this.userRepository.save(user);

        UserStatus status = new UserStatus(user.getId());
        this.userStatusRepository.save(status);

        log.info("{} has been created successfully. ✅ [ID: {}]", user.getNickname(), user.getId());
        return this.toResponse(user, status);
    }

    @Override
    public UserResponse findById(UUID id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND));

        return this.toResponse(user, status);
    }

    @Override
    public List<UserResponse> findAll() {
        return this.userRepository.findAll().stream()
                .map(user -> {
                    UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND));
                    return this.toResponse(user, status);
                })
                .toList();
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest, Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        if (userUpdateRequest.nickname() != null && !userUpdateRequest.nickname().isBlank())
            user.updateNickname(userUpdateRequest.nickname());

        if (userUpdateRequest.username() != null && !userUpdateRequest.username().isBlank()) {
            if (!user.getUsername().equals(userUpdateRequest.username()) && this.userRepository.existByUsername(userUpdateRequest.username()))
                throw new ApiException(USER_USERNAME_DUPLICATED);
            user.updateUsername(userUpdateRequest.username());
        }

        if (userUpdateRequest.email() != null && !userUpdateRequest.email().isBlank()) {
            if (!userUpdateRequest.email().matches(EMAIL_REGEX))
                throw new ApiException(USER_INVALID_EMAIL_FORMAT);
            if (!user.getEmail().equals(userUpdateRequest.email()) && this.userRepository.existByEmail(userUpdateRequest.email()))
                throw new ApiException(USER_EMAIL_DUPLICATED);
            user.updateEmail(userUpdateRequest.email());
        }

        if (userUpdateRequest.password() != null && !userUpdateRequest.password().isBlank()) {
            if (userUpdateRequest.password().length() < 8)
                throw new ApiException(USER_INVALID_PASSWORD_LENGTH);
            user.updatePassword(userUpdateRequest.password());
        }

        if (userUpdateRequest.phoneNumber() != null && !userUpdateRequest.phoneNumber().isBlank()) {
            if (!userUpdateRequest.phoneNumber().matches(PHONE_REGEX))
                throw new ApiException(USER_INVALID_PHONE_NUMBER_FORMAT);
            user.updatePhoneNumber(userUpdateRequest.phoneNumber());
        }

        BinaryContent profile = user.getProfileId() != null
                ? this.binaryContentRepository.findById(user.getProfileId())
                .orElseThrow(() -> new ApiException(BINARY_CONTENT_NOT_FOUND))
                : null;
        if (binaryContentCreateRequest.isPresent()) {
            if (profile != null) this.binaryContentRepository.delete(profile);
            BinaryContentCreateRequest req = binaryContentCreateRequest.get();
            profile = new BinaryContent(req.data(), req.fileName(), req.contentType(), req.size());
            this.binaryContentRepository.save(profile);
            user.updateProfileId(profile.getId());
        }

        this.userRepository.save(user);

        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND));

        status.setUpdatedAt();
        this.userStatusRepository.save(status);

        log.info("{} has been updated successfully. ✅ [ID: {}]", user.getNickname(), id);
        return this.toResponse(user, status);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        if (user.getProfileId() != null) {
            BinaryContent profile = this.binaryContentRepository.findById(user.getProfileId())
                    .orElseThrow(() -> new ApiException(BINARY_CONTENT_NOT_FOUND));
            this.binaryContentRepository.delete(profile);
        }

        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(USER_STATUS_NOT_FOUND));

        this.userStatusRepository.delete(status);

        this.readStatusRepository.deleteAllByUserId(user.getId());

        this.userRepository.delete(user);

        log.info("{} has been deleted successfully. ✅ [ID: {}]", user.getNickname(), id);
    }

    private UserResponse toResponse(User user, UserStatus status) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileId(),
                new UserStatusResponse(
                        status.getUpdatedAt(),
                        status.getUpdatedAt().isAfter(Instant.now().minusSeconds(5 * 60))
                )
        );
    }
}
