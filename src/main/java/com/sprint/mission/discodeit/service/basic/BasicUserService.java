package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateParam;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 username입니다.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 email입니다.");
        }

        BinaryContent profile = null;

        if (request.profileImage() != null) {
            profile = new BinaryContent(
                    request.profileImage().fileName(),
                    request.profileImage().bytes() == null ? 0 : request.profileImage().bytes().length,
                    request.profileImage().contentType()
            );

            profile = binaryContentRepository.save(profile);

            if (request.profileImage().bytes() != null) {
                binaryContentStorage.put(profile.getId(), request.profileImage().bytes());
            }
        }

        User user = new User(
                request.username(),
                request.email(),
                request.password()
        );

        if (profile != null) {
            user.updateProfile(profile);
        }

        User savedUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(savedUser, Instant.now());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);
        savedUser.updateStatus(savedUserStatus);

        return userMapper.toDto(savedUser);
    }

    @Override

    public Optional<UserDto> find(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto update(UserUpdateParam param) {
        User user = userRepository.findById(param.id())
                .orElseThrow(() -> new NotFoundException("해당 사용자가 존재하지 않습니다."));

        String newUsername = user.getUsername();
        String newEmail = user.getEmail();
        String newPassword = user.getPassword();

        if (param.request().newUsername() != null) {
            validateUsername(param.request().newUsername(),user);
            newUsername = param.request().newUsername();
        }

        if (param.request().newEmail() != null) {
            validateEmail(param.request().newEmail(), user);
            newEmail = param.request().newEmail();
        }

        if (param.request().newPassword() != null) {
            newPassword = param.request().newPassword();
        }

        user.update(newUsername, newEmail, newPassword);

        if (param.request().newProfileImage() != null){
            replaceProfileImage(user, param.request().newProfileImage());
        }
        return userMapper.toDto(user);
        }

    private void validateUsername(String newUsername, User user) {
        Optional<User> userByUsername = userRepository.findByUsername(newUsername);
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 username입니다.");
        }
    }

    private void validateEmail(String newEmail, User user) {
        Optional<User> userByEmail = userRepository.findByEmail(newEmail);
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 email입니다.");
        }
    }

    private void replaceProfileImage(User user, BinaryContentCreateRequest newProfileImage) {
        if (user.getProfile() != null) {
            binaryContentRepository.deleteById(user.getProfile().getId());
        }

        BinaryContent newProfile = new BinaryContent(
                newProfileImage.fileName(),
                newProfileImage.bytes() == null ? 0 : newProfileImage.bytes().length,
                newProfileImage.contentType()
        );

        BinaryContent savedProfile = binaryContentRepository.save(newProfile);

        if (newProfileImage.bytes() != null) {
            binaryContentStorage.put(savedProfile.getId(), newProfileImage.bytes());
        }

        user.updateProfile(savedProfile);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        if (user.getProfile() != null) {
            binaryContentRepository.deleteById(user.getProfile().getId());
        }

        userStatusRepository.findByUser_Id(user.getId())
                .ifPresent(status -> userStatusRepository.deleteById(status.getId()));

        userRepository.deleteById(id);
    }
}