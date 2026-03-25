package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateDto;
import com.sprint.mission.discodeit.dto.user.UserInfoDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateDto;
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
public class BasicUserService implements UserService{

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;


    @Override
    public User create(UserCreateDto dto) {
        boolean isDuplicated = userRepository.findAll().values().stream()
                .anyMatch(u -> u.getName().equals(dto.name()) || u.getEmail().equals(dto.email()));
        if (isDuplicated) {
            throw new IllegalArgumentException("이미 가입된 유저입니다.");
        }

        UUID profileImageId = null;
        if (dto.profileImage() != null) {
            BinaryContent image = new BinaryContent(dto.profileImage());
            binaryContentRepository.save(image);
            profileImageId = image.getId();
        }

        User user = new User(dto.name(), dto.email(), dto.password(), profileImageId);

        UserStatus status = new UserStatus(user.getId());
        userStatusRepository.save(status);

        return userRepository.save(user);
    }

    @Override
    public List<UserInfoDto> findAll() {
        List<User> users = userRepository.findAll().values().stream().toList();

        return users.stream()
                .map(u -> {
                    UserStatus status = userStatusRepository.findByUserId(u.getId())
                            .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));
                    return new UserInfoDto(u.getId(), u.getName(), u.getEmail(), u.getProfileImageId(), status.isOnline());
                })
                .toList();
    }

    @Override
    public UserInfoDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        return new UserInfoDto(user.getId(), user.getName(), user.getEmail(), user.getProfileImageId(), status.isOnline());
    }

    @Override
    public UserInfoDto update(UserUpdateDto dto) {
        User user = userRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));

        boolean isDuplicated = userRepository.findAll().values().stream()
                // 새로 받은 정보가 나를 제외하고, 다른 객체의 이름 및 이메일과 다른지 검시
                .filter(u -> !u.getId().equals(dto.id()))
                .anyMatch(u -> u.getName().equals(dto.name())
                        || u.getEmail().equals(dto.email()));
        if (isDuplicated) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임/이메일입니다.");
        }

        UUID newProfileImageId = user.getProfileImageId();
        if (dto.profileImage() != null) {
            if (newProfileImageId != null) {
                binaryContentRepository.delete(newProfileImageId);
            }
            BinaryContent image = new BinaryContent(dto.profileImage());
            binaryContentRepository.save(image);
            newProfileImageId = image.getId();
        }
        // 일반 User 객체도 update 해주어야 함
        user.update(dto.name(), dto.email(), dto.password(), newProfileImageId);
        userRepository.save(user);

        // findByUserId()를 사용해야 함
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        return new UserInfoDto(user.getId(),
                user.getName(), user.getEmail(), newProfileImageId, status.isOnline());
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        if (user.getProfileImageId() != null) {
            binaryContentRepository.delete(user.getProfileImageId());
        }

        userStatusRepository.delete(status.getId());
        userRepository.delete(user.getId());
    }
}
