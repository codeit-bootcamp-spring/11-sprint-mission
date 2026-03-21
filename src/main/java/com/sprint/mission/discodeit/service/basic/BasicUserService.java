package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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
    private final ChannelRepository channelRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^\\d{3}-\\d{3}-\\d{4}$";

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest, Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
        if (userCreateRequest.nickname() == null || userCreateRequest.nickname().isBlank())
            throw new IllegalArgumentException("nickname is required. ❌");

        if (userCreateRequest.username() == null || userCreateRequest.username().isBlank())
            throw new IllegalArgumentException("username is required. ❌");
        if (this.existUserByUsername(userCreateRequest.username()))
            throw new IllegalArgumentException("username cannot be duplicated. ❌");

        if (userCreateRequest.email() == null || userCreateRequest.email().isBlank())
            throw new IllegalArgumentException("email is required. ❌");
        if (!userCreateRequest.email().matches(EMAIL_REGEX))
            throw new IllegalArgumentException("email format is invalid. ❌");
        if (this.existUserByEmail(userCreateRequest.email()))
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

        UserStatus userStatus = new UserStatus(user.getId());
        this.userStatusRepository.save(userStatus);

        log.info("{} has been created successfully. ✅ [ID: {}]", user.getNickname(), user.getId());
        return user.toResponse(profile, userStatus);
    }

    @Override
    public UserResponse findById(UUID id) {
        User user = this.userRepository.findById(id);
        BinaryContent profile = user.getProfileId() != null
                ? this.binaryContentRepository.findById(user.getProfileId())
                : null;
        UserStatus status = this.userStatusRepository.findByUserId(user.getId());
        return user.toResponse(profile, status);
    }

    @Override
    public boolean existUserByUsername(String username) {
        return this.userRepository.existByUsername(username);
    }

    @Override
    public boolean existUserByEmail(String email) {
        return this.userRepository.existByEmail(email);
    }

    @Override
    public List<UserResponse> findAll() {
        return this.userRepository.findAll().stream()
                .map(u -> {
                    BinaryContent profile = u.getProfileId() != null
                            ? this.binaryContentRepository.findById(u.getProfileId())
                            : null;
                    UserStatus userStatus = this.userStatusRepository.findByUserId(u.getId());
                    return u.toResponse(profile, userStatus);
                })
                .toList();
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest, Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
        User user = this.userRepository.findById(id);

        if (userUpdateRequest.nickname() != null && !userUpdateRequest.nickname().isBlank())
            user.updateNickname(userUpdateRequest.nickname());
        if (userUpdateRequest.username() != null && !userUpdateRequest.username().isBlank()) {
            if (!user.getUsername().equals(userUpdateRequest.username()) && this.existUserByUsername(userUpdateRequest.username()))
                throw new IllegalArgumentException("username cannot be duplicated. ❌");
            user.updateUsername(userUpdateRequest.username());
        }
        if (userUpdateRequest.email() != null && !userUpdateRequest.email().isBlank()) {
            if (!userUpdateRequest.email().matches(EMAIL_REGEX))
                throw new IllegalArgumentException("email format is invalid. ❌");
            if (!user.getEmail().equals(userUpdateRequest.email()) && this.existUserByEmail(userUpdateRequest.email()))
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
                : null;
        if (binaryContentCreateRequest.isPresent()) {
            profile = new BinaryContent(binaryContentCreateRequest.get());
            this.binaryContentRepository.save(profile);
            user.updateProfile(profile);
        }

        this.userRepository.save(user);

        UserStatus userStatus = this.userStatusRepository.findByUserId(user.getId());
        userStatus.setUpdatedAt();
        this.userStatusRepository.save(userStatus);

        log.info("{} has been updated successfully. ✅ [ID: {}]", user.getNickname(), id);
        return user.toResponse(profile, userStatus);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = this.userRepository.findById(id);

        user.getChannels().
                forEach(channel -> {
                    channel.getParticipants().removeIf(participant -> participant.getId().equals(user.getId()));
                    this.channelRepository.save(channel);
                });

        if (user.getProfileId() != null) {
            BinaryContent profile = this.binaryContentRepository.findById(user.getProfileId());
            this.binaryContentRepository.delete(profile);
        }

        UserStatus userStatus = this.userStatusRepository.findByUserId(user.getId());
        this.userStatusRepository.delete(userStatus);

        this.userRepository.delete(user);

        log.info("{} has been deleted successfully and left from all channels. ✅ [ID: {}]", user.getNickname(), id);
    }
}
