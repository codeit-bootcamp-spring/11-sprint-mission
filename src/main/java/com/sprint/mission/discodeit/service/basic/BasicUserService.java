package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserDto createUser(UserCreateRequest request) {
        String name = request.name().trim();
        String email = request.email().trim();
        String password = request.password().trim();
        if(userRepository.findByUserName(name).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }
        if(userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다. 다른 이메일을 사용해주세요.");
        }
        User newUser = new User(name, email, password, request.profileImageId());
        UserStatus newStatus = new UserStatus(newUser.getId());
        try {
            userRepository.save(newUser);
            userStatusRepository.save(newStatus);
        } catch (Exception e) {
            throw  new RuntimeException("시스템 오류로 가입에 실패했습니다.");
        }

        return UserDto.from(newUser, newStatus);
    }

    @Override
    public UserDto readUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("읽어 오려는 유저가 존재하지 않습니다."));
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("유저 상태 정보가 없습니다."));

        return UserDto.from(user, status);
    }

    @Override
    public List<UserDto> allReadUser() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {UserStatus status = userStatusRepository.findByUserId(user.getId())
                        .orElseGet(() -> new UserStatus(user.getId()));
                return UserDto.from(user, status);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(UUID id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 유저가 존재하지 않습니다."));
        if(!user.getPassword().equals(password)) {
            throw new InputMismatchException("패스워드가 일치하지 않습니다.");
        }

        if(user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }
        userStatusRepository.deleteByUserId(id);
        userRepository.delete(id);
    }

    @Override
    public void updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("검색하신 유저가 존재하지 않습니다."));
        String name = user.getUsername();
        if(request.name() != null) {
            userRepository.findByUserName(request.name())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {throw new IllegalArgumentException("이미 사용중인 이름 입니다.");});
            name = request.name();
        }
        String email = user.getEmail();
        if(request.email() != null) {
            userRepository.findByEmail(email)
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");});
            email = request.email();
        }

        String password = (request.password() != null) ? request.password() : user.getPassword();

        UUID profileId = user.getProfileId();

        if(request.profileImageId() != null) {
            if(user.getProfileId() != null && !user.getProfileId().equals(request.profileImageId())) {
                binaryContentRepository.delete(user.getProfileId());
            }
            profileId = request.profileImageId();
        }

        user.update(name, email, password, profileId);
        userRepository.save(user);
    }
}
