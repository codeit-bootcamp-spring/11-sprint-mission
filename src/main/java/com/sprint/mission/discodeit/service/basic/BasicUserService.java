package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final ReadStatusRepository readStatusRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^\\d{3}-\\d{3}-\\d{4}$";

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest, Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
        if (userCreateRequest.nickname() == null || userCreateRequest.nickname().isBlank())
            throw new IllegalArgumentException("nickname is required. ❌");

        if (userCreateRequest.username() == null || userCreateRequest.username().isBlank())
            throw new IllegalArgumentException("username is required. ❌");
        if (this.userRepository.existByUsername(userCreateRequest.username()))
            throw new IllegalArgumentException("username cannot be duplicated. ❌");

        if (userCreateRequest.email() == null || userCreateRequest.email().isBlank())
            throw new IllegalArgumentException("email is required. ❌");
        if (!userCreateRequest.email().matches(EMAIL_REGEX))
            throw new IllegalArgumentException("email format is invalid. ❌");
        if (this.userRepository.existByEmail(userCreateRequest.email()))
            throw new IllegalArgumentException("email cannot be duplicated. ❌");

        if (userCreateRequest.password() == null || userCreateRequest.password().isBlank())
            throw new IllegalArgumentException("password is required. ❌");
        if (userCreateRequest.password().length() < 8)
            throw new IllegalArgumentException("password length should be at least 8 characters. ❌");

        if (userCreateRequest.phoneNumber() == null || userCreateRequest.phoneNumber().isBlank())
            throw new IllegalArgumentException("phone number is required. ❌");
        if (!userCreateRequest.phoneNumber().matches(PHONE_REGEX))
            throw new IllegalArgumentException("phone number format is invalid. ❌");

        BinaryContent profile = null;
        if (binaryContentCreateRequest.isPresent()) {
            profile = new BinaryContent(binaryContentCreateRequest.get());
            this.binaryContentRepository.save(profile);
        }

        User user = new User(userCreateRequest, profile);
        this.userRepository.save(user);

        UserStatus status = new UserStatus(user);
        this.userStatusRepository.save(status);

        log.info("{} has been created successfully. ✅ [ID: {}]", user.getNickname(), user.getId());
        return user.toResponse(profile, status);
    }

    @Override
    public UserResponse findById(UUID id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌"));
        BinaryContent profile = user.getProfileId() != null
                ? this.binaryContentRepository.findById(user.getProfileId())
                .orElseThrow(() -> new IllegalArgumentException("requested binary content not found. ❌"))
                : null;
        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));
        return user.toResponse(profile, status);
    }

    @Override
    public List<UserResponse> findAll() {
        return this.userRepository.findAll().stream()
                .map(user -> {
                    BinaryContent profile = user.getProfileId() != null
                            ? this.binaryContentRepository.findById(user.getProfileId())
                            .orElseThrow(() -> new IllegalArgumentException("requested binary content not found. ❌"))
                            : null;
                    UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));
                    return user.toResponse(profile, status);
                })
                .toList();
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest, Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌"));

        if (userUpdateRequest.nickname() != null && !userUpdateRequest.nickname().isBlank())
            user.updateNickname(userUpdateRequest.nickname());
        if (userUpdateRequest.username() != null && !userUpdateRequest.username().isBlank()) {
            if (!user.getUsername().equals(userUpdateRequest.username()) && this.userRepository.existByUsername(userUpdateRequest.username()))
                throw new IllegalArgumentException("username cannot be duplicated. ❌");
            user.updateUsername(userUpdateRequest.username());
        }
        if (userUpdateRequest.email() != null && !userUpdateRequest.email().isBlank()) {
            if (!userUpdateRequest.email().matches(EMAIL_REGEX))
                throw new IllegalArgumentException("email format is invalid. ❌");
            if (!user.getEmail().equals(userUpdateRequest.email()) && this.userRepository.existByEmail(userUpdateRequest.email()))
                throw new IllegalArgumentException("email cannot be duplicated. ❌");
            user.updateEmail(userUpdateRequest.email());
        }
        if (userUpdateRequest.password() != null && !userUpdateRequest.password().isBlank()) {
            if (userUpdateRequest.password().length() < 8)
                throw new IllegalArgumentException("password length should be at least 8 characters. ❌");
            user.updatePassword(userUpdateRequest.password());
        }
        if (userUpdateRequest.phoneNumber() != null && !userUpdateRequest.phoneNumber().isBlank()) {
            if (!userUpdateRequest.phoneNumber().matches(PHONE_REGEX))
                throw new IllegalArgumentException("phone number format is invalid. ❌");
            user.updatePhoneNumber(userUpdateRequest.phoneNumber());
        }

        BinaryContent profile = user.getProfileId() != null
                ? this.binaryContentRepository.findById(user.getProfileId())
                .orElseThrow(() -> new IllegalArgumentException("requested binary content not found. ❌"))
                : null;
        if (binaryContentCreateRequest.isPresent()) {
            profile = new BinaryContent(binaryContentCreateRequest.get());
            this.binaryContentRepository.save(profile);
            user.updateProfile(profile);
        }

        this.userRepository.save(user);

        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));
        status.setUpdatedAt();
        this.userStatusRepository.save(status);

        log.info("{} has been updated successfully. ✅ [ID: {}]", user.getNickname(), id);
        return user.toResponse(profile, status);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested user not found. ❌"));

        if (user.getProfileId() != null) {
            BinaryContent profile = this.binaryContentRepository.findById(user.getProfileId())
                    .orElseThrow(() -> new IllegalArgumentException("requested binary content not found. ❌"));
            this.binaryContentRepository.delete(profile);
        }

        UserStatus status = this.userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("requested user status not found. ❌"));
        this.userStatusRepository.delete(status);

        this.readStatusRepository.deleteAllByUserId(user.getId());

        this.userRepository.delete(user);

        log.info("{} has been deleted successfully and left from all channels. ✅ [ID: {}]", user.getNickname(), id);
    }
}
