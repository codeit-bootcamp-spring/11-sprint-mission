package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;


    @Override
    public UserDto.Response create(UserDto.CreateRequest request) {
        // username 중복 확인
        if (userRepository.existsByName(request.userName())) {
            throw new IllegalArgumentException("User with name " + request.userName() + " already exists");
        }

        // email 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        // toEntity()로 유저 등록
        User user = request.toEntity();
        userRepository.save(user);

        // UserStatus 함께 생성
        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return UserDto.Response.of(user, userStatus);
    }

    @Override
    public UserDto.Response findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("UserStatus with id " + id + " not found"));

        return UserDto.Response.of(user, userStatus);
    }

    @Override
    public List<UserDto.Response> findAll() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    UserStatus status = userStatusRepository.findById(user.getId())
                            .orElseGet(() -> new UserStatus(user.getId()));
                    return UserDto.Response.of(user, status);
                })
                .toList();
    }

    @Override
    public UserDto.Response update(UUID id, UserDto.UpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));

        user.update(
                request.username(),
                request.nickname(),
                request.description(),
                request.email(),
                request.password()
        );
        user.updateProfileImage(request.profileImageId());

        userRepository.save(user);

        // 상태 정보 반환
        UserStatus status = userStatusRepository.findById(id)
                .orElseGet(() -> new UserStatus(id));

        return UserDto.Response.of(user, status);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));


        userStatusRepository.deleteById(id);

        // 프로필 이미지 삭제 (존재 시)
        if (user.getProfileImageId() != null) {
            binaryContentRepository.deleteById(user.getProfileImageId());
        }
        userRepository.deleteById(id);
    }
}