package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveException;
import com.sprint.mission.discodeit.exception.user.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.user.DuplicateNameException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusOfUserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public UserDto create(UserCreateRequest dto, MultipartFile profile) {
        if(userRepo.findByName(dto.username()).isPresent()) throw new DuplicateNameException(dto.username());
        if(userRepo.findByEmail(dto.email()).isPresent()) throw new DuplicateEmailException(dto.email());

        UUID profileId = saveProfile(profile);

        User user = new User(dto.username(), dto.email(), dto.password(), profileId);
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
    public void update(UUID id, UserUpdateRequest dto, MultipartFile profile) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if(!user.getUsername().equals(dto.newUsername()) && userRepo.findByName(dto.newUsername()).isPresent()) throw new DuplicateNameException(dto.newUsername());
        if(!user.getEmail().equals(dto.newEmail()) && userRepo.findByEmail(dto.newEmail()).isPresent()) throw new DuplicateEmailException(dto.newEmail());

        UUID oldprofileId = user.getProfileId();
        UUID newprofileId = saveProfile(profile);

        user.setUsername(dto.newUsername());
        user.setEmail(dto.newEmail());
        user.setPassword(dto.newPassword());
        if(newprofileId != null) user.setProfileId(newprofileId);

        user.update();
        userRepo.save(user);

        if(newprofileId != null && oldprofileId != null) {
            binaryContentRepo.deleteById(oldprofileId);
        }
    }

    @Override
    public void delete(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserStatus userStatus = userStatusRepo.findByUserId(id)
                .orElseThrow(() -> new UserStatusOfUserNotFoundException(id));
        userStatusRepo.deleteById(userStatus.getId());

        UUID profileId = user.getProfileId();
        if(profileId != null) {
            if(!binaryContentRepo.deleteById(profileId)) {
                throw new BinaryContentNotFoundException(profileId);
            }
        }
        if(!userRepo.deleteById(id)) {
            throw new UserNotFoundException(id);
        }
    }

    private UUID saveProfile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            return null;
        }

        try {
            BinaryContent binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            binaryContentRepo.save(binaryContent);
            return binaryContent.getId();
        } catch (IOException e){
            throw new BinaryContentSaveException(file.getOriginalFilename(), e);
        }
    }
}
