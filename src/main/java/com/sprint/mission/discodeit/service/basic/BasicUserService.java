package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.user.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.user.DuplicateNameException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusOfUserNotFoundException;
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

    private final UserRepository userRepo;
    private final UserStatusRepository userStatusRepo;
    private final BinaryContentRepository binaryContentRepo;

    @Override
    public UserDto create(UserCreateRequest dto) {
        if(userRepo.existsByName(dto.username())) throw new DuplicateNameException(dto.username());
        if(userRepo.existsByEmail(dto.email())) throw new DuplicateEmailException(dto.email());

        User user = new User(dto.username(), dto.email(), dto.password(), dto.profileId());
        userRepo.save(user);

        UserStatus userStatus = new UserStatus(user.getId(), Instant.now());
        userStatusRepo.save(userStatus);

        return new UserDto(user.getId(), user.getCreatedAt(), user.getUpdatedAt(),
                user.getUsername(), user.getEmail(), user.getProfileId(), userStatus.passed());
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        UserStatus userStatus = userStatusRepo.findByUserId(id)
                .orElseThrow(() -> new UserStatusOfUserNotFoundException(id));

        return new UserDto(user.getId(), user.getCreatedAt(), user.getUpdatedAt(),
                user.getUsername(), user.getEmail(), user.getProfileId(), userStatus.passed());
    }

    @Override
    public List<UserDto> findAll() {
        return userRepo.findAll().stream()
                .map(user -> {
                            UserStatus userStatus = userStatusRepo.findByUserId(user.getId())
                                    .orElseThrow(() -> new UserStatusOfUserNotFoundException(user.getId()));

                            return new UserDto(
                                    user.getId(),
                                    user.getCreatedAt(),
                                    user.getUpdatedAt(),
                                    user.getUsername(),
                                    user.getEmail(),
                                    user.getProfileId(),
                                    userStatus.passed()
                            );
                        }
                )
                .toList();
    }

    @Override
    public void update(UUID id, UserUpdateRequest dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if(!user.getUsername().equals(dto.newUsername()) && userRepo.existsByName(dto.newUsername())) throw new DuplicateNameException(dto.newUsername());
        if(!user.getEmail().equals(dto.newEmail()) && userRepo.existsByEmail(dto.newEmail())) throw new DuplicateEmailException(dto.newEmail());

        user.setUsername(dto.newUsername());
        user.setEmail(dto.newEmail());
        user.setPassword(dto.newPassword());
        if(dto.newProfileId() != null) user.setProfileId(dto.newProfileId());
        user.update();

        userRepo.save(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userStatusRepo.deleteByUserId(id);

        if(user.getProfileId() != null) {
            BinaryContent binaryContent = binaryContentRepo.findById(user.getProfileId())
                    .orElseThrow(() -> new BinaryContentNotFoundException(user.getProfileId()));
            binaryContentRepo.delete(binaryContent);
        }

        userRepo.delete(user);
    }
}
