package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserStatusRepository;
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
    private final FileUserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserDto createUser(UserCreateRequest request) {
        String name = request.name();
        String email = request.email();
        String password = request.password();
        if(userRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }
        if(userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다. 다른 이메일을 사용해주세요.");
        }
        User newUser = new User(name, email, password);
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
                        .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
                return UserDto.from(user, status);
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserDto deleteUser(UUID id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 유저가 존재하지 않습니다."));
        if(!user.getPassword().equals(password)) {
            throw new InputMismatchException("패스워드가 일치하지 않습니다.");
        }

        UserStatus status = userStatusRepository.findByUserId(id).orElse(null);
        UserDto response = UserDto.from(user, status);

        if(user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }
        userStatusRepository.deleteByUserId(id);
        userRepository.delete(id);

        return response;
    }

    @Override
    public void updateUser(UUID id, UserUpdateRequest request) {
        String name = request.name();
        String email = request.email();
        String password = request.password();
        byte[] profileImage = request.profileImage();

        User user = userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("검색하신 유저가 존재하지 않습니다."));
        userRepository.findByName(name)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {throw new IllegalArgumentException("이미 사용중인 이름 입니다.");});
        userRepository.findByEmail(email)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");});
        if(profileImage != null) {
            if(user.getProfileId() != null) {
                binaryContentRepository.delete(user.getProfileId());
            }
            BinaryContent content = new BinaryContent(profileImage);

            binaryContentRepository.save(content);
            user.setProfileId(content.getId());
        }

        user.update(name, email, password);
        userRepository.save(user);
    }
}
