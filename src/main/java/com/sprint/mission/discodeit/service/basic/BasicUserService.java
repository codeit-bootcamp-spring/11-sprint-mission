package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.UserOnlineStatusResolver;
import com.sprint.mission.discodeit.security.authority.UserRole;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;
    private final PasswordEncoder passwordEncoder;
    private final UserOnlineStatusResolver userOnlineStatusResolver;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest request) {
        log.info("사용자 생성 요청: username={}, email={}",
                request.username(),
                request.email()
        );

        if (userRepository.existsByUsername(request.username())) {
            log.warn("사용자 생성 실패 - 중복 username: {}", request.username());
            throw new UserAlreadyExistsException("username", request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("사용자 생성 실패 - 중복 email: {}", request.email());
            throw new UserAlreadyExistsException("email", request.email());
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
                eventPublisher.publishEvent(
                        new BinaryContentCreatedEvent(profile.getId(), request.profileImage().bytes())
                );
            }
        }

        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        if (profile != null) {
            user.updateProfile(profile);
        }

        User savedUser = userRepository.save(user);

        log.info("사용자 생성 완료: userId={}", savedUser.getId());
        return userMapper.toDto(savedUser, Set.of());
    }

    @Override
    public Optional<UserDto> find(UUID id) {
        Set<UUID> onlineUserIds =
                userOnlineStatusResolver.getOnlineUserIds();

        return userRepository.findById(id)
                .map(user -> userMapper.toDto(user, onlineUserIds));
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("사용자 목록 조회 처리 시작");

        Set<UUID> onlineUserIds =
                userOnlineStatusResolver.getOnlineUserIds();

        List<UserDto> users = userRepository.findAll().stream()
                .map(user -> userMapper.toDto(user, onlineUserIds))
                .toList();

        log.debug("사용자 목록 조회 완료: count={}", users.size());

        return users;
    }

    @Override
    @Transactional
    @PreAuthorize("@userSecurity.isSelf(#param.id, authentication)")
    public UserDto update(UserUpdateParam param) {
        log.info("사용자 수정 처리 시작: userId={}", param.id());

        User user = userRepository.findById(param.id())
                .orElseThrow(() -> {
                    log.warn("사용자 수정 실패 - 사용자가 존재하지 않음: userId={}", param.id());
                    return new UserNotFoundException(param.id());
                });

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
            newPassword = passwordEncoder.encode(
                    param.request().newPassword()
            );
        }

        user.update(newUsername, newEmail, newPassword);

        if (param.request().newProfileImage() != null){
            replaceProfileImage(user, param.request().newProfileImage());
        }

        log.info("사용자 수정 처리 완료: userId={}", user.getId());
        Set<UUID> onlineUserIds = userOnlineStatusResolver.getOnlineUserIds();
        return userMapper.toDto(user, onlineUserIds);
    }

    private void validateUsername(String newUsername, User user) {
        Optional<User> userByUsername = userRepository.findByUsername(newUsername);
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(user.getId())) {
            log.warn("사용자 수정 실패 - 중복 username: userId={}, username={}",
                    user.getId(),
                    newUsername
            );
            throw new UserAlreadyExistsException("username", newUsername);
        }
    }

    private void validateEmail(String newEmail, User user) {
        Optional<User> userByEmail = userRepository.findByEmail(newEmail);
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(user.getId())) {
            log.warn("사용자 수정 실패 - 중복 email: userId={}, email={}",
                    user.getId(),
                    newEmail
            );
            throw new UserAlreadyExistsException("email", newEmail);
        }
    }

    private void replaceProfileImage(User user, BinaryContentCreateRequest newProfileImage) {
        log.debug("사용자 프로필 이미지 교체 시작: userId={}", user.getId());

        if (user.getProfile() != null) {
            UUID oldProfileId = user.getProfile().getId();
            binaryContentRepository.deleteById(oldProfileId);
            binaryContentStorage.delete(oldProfileId);

            log.debug("기존 프로필 이미지 삭제 완료: userId={}, oldProfileId={}",
                    user.getId(),
                    oldProfileId
            );
        }

        BinaryContent newProfile = new BinaryContent(
                newProfileImage.fileName(),
                newProfileImage.bytes() == null ? 0 : newProfileImage.bytes().length,
                newProfileImage.contentType()
        );

        BinaryContent savedProfile = binaryContentRepository.save(newProfile);

        if (newProfileImage.bytes() != null) {
            eventPublisher.publishEvent(
                    new BinaryContentCreatedEvent(savedProfile.getId(), newProfileImage.bytes())
            );
        }

        user.updateProfile(savedProfile);

        log.debug("새 프로필 이미지 저장 완료: userId={}, profileId={}",
                user.getId(),
                savedProfile.getId()
        );
    }

    @Override
    @Transactional
    @PreAuthorize("@userSecurity.isSelf(#id, authentication)")
    public void delete(UUID id) {
        log.info("사용자 삭제 처리 시작: userId={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("사용자 삭제 실패 - 사용자 없음: userId={}", id);
                    return new UserNotFoundException(id);
                });

        if (user.getProfile() != null) {
            UUID profileId = user.getProfile().getId();
            binaryContentRepository.deleteById(profileId);
            binaryContentStorage.delete(profileId);
        }

        userRepository.deleteById(id);

        log.info("사용자 삭제 완료: userId={}", id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDto updateRole(UserRoleUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        UserRole previousRole = user.getRole();

        user.updateRole(request.newRole());

        eventPublisher.publishEvent(
                new RoleUpdatedEvent(user.getId(), previousRole, request.newRole())
        );

        userOnlineStatusResolver.invalidateTokens(user.getId());

        return userMapper.toDto(user, Set.of());
    }
}
