package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw DiscodeitException.duplicateUser(request.getUserName());
        }
        if (userRepository.existsByEmail(request.getUserEmail())) {
            throw DiscodeitException.duplicateEmail(request.getUserEmail());
        }

        User user = new User(request.getUserName(), request.getUserEmail(), request.getUserPassword());
        user.validateService();
        userRepository.create(user);

        if (request.getFileName() != null) {
            BinaryContent binaryContent = BinaryContent.forProfile(user.getId(), request.getFileName(), request.getFileContent(),request.getContentType());
            binaryContentRepository.create(binaryContent);
        }

        UserStatus userStatus = new UserStatus(user.getId(), Instant.now());
        userStatusRepository.create(userStatus);

        return new UserResponse(user.getId(), user.getUserName(), user.getUserEmail(), userStatus.isOnline());
    }

    @Override
    public UserResponse read(UUID id) {
        User user = userRepository.read(id);
        if(user == null) throw DiscodeitException.userNotFound(id);
        UserStatus userStatus = userStatusRepository.readByUserId(id);
        return new UserResponse(user.getId(), user.getUserName(), user.getUserEmail(), userStatus.isOnline());
    }

    @Override
    public List<UserResponse> readAll() {
        return userRepository.readAll().stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.readByUserId(user.getId());
                    return new UserResponse(user.getId(), user.getUserName(), user.getUserEmail(), userStatus.isOnline());
                })
                .toList();

    }

    @Override
    public void update(UserUpdateRequest request) {
        User user = userRepository.read(request.getId());
        if (user == null) throw DiscodeitException.userNotFound(request.getId());

        if (userRepository.existsByUserNameExcluding(request.getUserName(), request.getId())) {
            throw DiscodeitException.duplicateUser(request.getUserName());
        }
        if (userRepository.existsByEmailExcluding(request.getUserEmail(), request.getId())) {
            throw DiscodeitException.duplicateEmail(request.getUserEmail());
        }
        if(request.getFileName() != null){
            binaryContentRepository.deleteByUserId(user.getId());
            BinaryContent binaryContent = BinaryContent.forProfile(user.getId(), request.getFileName(), request.getFileContent(),request.getContentType());
            binaryContentRepository.create(binaryContent);
        }

        user.updateUser(request.getUserName(), request.getUserEmail(), request.getUserPassword());
        user.validateService();
        userRepository.update(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.read(id);
        if (user == null) {
            throw DiscodeitException.userNotFound(id);
        }
        binaryContentRepository.deleteByUserId(id);
        userStatusRepository.delete(id);
        userRepository.delete(id);
    }

    @Override
    public void restore(UUID id) {
        userRepository.restore(id);
        userStatusRepository.restore(id);
    }

    @Override
    public List<UserDto> readAllDto(){
        return userRepository.readAll().stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.readByUserId(user.getId());
                    BinaryContent profile = binaryContentRepository.readByUserId(user.getId());
                    UUID profileId = profile == null ? null: profile.getId();
                    return new UserDto(
                            user.getId(),
                            user.getCreatedAt(),
                            user.getUpdatedAt(),
                            user.getUserName(),
                            user.getUserEmail(),
                            profileId,
                            userStatus.isOnline()
                    );
                })
                .toList();
    }
}