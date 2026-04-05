package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponseDto create(UserCreateRequestDto dto) {
        // user, email 중복체크
        if (userRepository.existsByUsername(dto.getUsername()) ||
                userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("username 또는 email이 중복됩니다.");
        }
        // user 생성 및 저장
        User user = new User(dto.getUsername(), dto.getEmail(), dto.getPassword());
        userRepository.save(user);

        // UserStatus 생성 및 저장 (최초 생성시 온라인 처리)
        UserStatus userStatus = new UserStatus(user.getId(), Instant.now());
        userStatusRepository.save(userStatus);

        // 프로필 이미지 저장(선택)
        if (dto.getProfileImageContent() != null && dto.getProfileImageContentType() != null) {
            BinaryContent profile = new BinaryContent(
                    "text.png", dto.getProfileImageContent(),
                    dto.getProfileImageContentType(),
                    user.getId(),
                    null
            );
            binaryContentRepository.save(profile);
        }
        return toResponseDto(user, userStatus);
    }

    @Override
    public UserResponseDto findById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 유저 없음"));
        UserStatus userStatus = userStatusRepository.findByUserId(id).orElse(null);
        return toResponseDto(user, userStatus);
    }

    @Override
    public List<UserResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    List<UserStatus> statusList = userStatusRepository.findAllByUserId(user.getId());
                    UserStatus userStatus = statusList.isEmpty() ? null : statusList.get(0);
                    return toResponseDto(user, userStatus);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getOnline()
                ))
                .collect(Collectors.toList());

    }

    @Override
    public UserResponseDto update(UUID id, UserUpdateRequestDto dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음"));
        user.update(dto.getUsername(), dto.getEmail(), dto.getPassword(), dto.getProfileImageContent(), dto.getProfileImageContentType());
        userRepository.save(user);

        if (dto.getProfileImageContent() != null && dto.getProfileImageContentType() != null) {
            BinaryContent profile = new BinaryContent(
                    "text.png", dto.getProfileImageContent(),
                    dto.getProfileImageContentType(),
                    user.getId(),
                    null
            );
            binaryContentRepository.save(profile);
        }
        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
        return toResponseDto(user, userStatus);
    }

    @Override
    public void delete(UUID id) {
        // 프로필 이미지 삭제
        binaryContentRepository.findAllByIdIn(List.of(id)).forEach(bc -> {
            binaryContentRepository.deleteById(bc.getId());
        });
        // UserStatus 삭제
        userStatusRepository.findAllByUserId(id).forEach(us -> {
            userStatusRepository.deleteById(us.getId());
        });
        // User삭제
        userRepository.deleteById(id);
    }

    private UserResponseDto toResponseDto(User user, UserStatus userStatus) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setOnline(userStatus != null && userStatus.isOnline());
        return dto;
    }

    @Override
    public boolean login(LoginRequestDto dto) {
        Optional<User> optionalUser = userRepository.findByUsername(dto.getUsername());
        if (optionalUser.isEmpty()) return false;
        User user = optionalUser.get();

        return user.getPassword().equals(dto.getPassword());
    }

    @Override
    public UserStatusResponseDto updateOnlineStatus(UUID id, UserStatusResponseDto dto) {
        UserStatus status = userStatusRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException("상태 정보를 찾을 수 없습니다."));

        status.setLastAccessAt(Instant.now());
        userStatusRepository.save(status);
        return new UserStatusResponseDto(status);
    }
}


