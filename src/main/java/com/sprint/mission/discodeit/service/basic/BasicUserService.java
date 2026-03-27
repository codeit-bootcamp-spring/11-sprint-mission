package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
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
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }
        if(userRepository.findByEmail(email).isPresent()) {
            throw new BusinessLogicException(ExceptionCode.EMAIL_EXISTS);
        }
        User newUser = new User(name, email, password, request.profileImageId());
        UserStatus newStatus = new UserStatus(newUser.getId());
        try {
            userRepository.save(newUser);
            userStatusRepository.save(newStatus);
        } catch (Exception e) {
            throw  new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }

        return UserDto.from(newUser, newStatus);
    }

    @Override
    public UserDto readUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_STATUS_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
        if(!user.getPassword().equals(password)) {
            throw new BusinessLogicException(ExceptionCode.PASSWORD_NOT_MATCH);
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
                        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        String name = user.getUsername();
        if(request.name() != null) {
            userRepository.findByUserName(request.name())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);});
            name = request.name();
        }
        String email = user.getEmail();
        if(request.email() != null) {
            userRepository.findByEmail(email)
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {throw new BusinessLogicException(ExceptionCode.EMAIL_EXISTS);});
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
