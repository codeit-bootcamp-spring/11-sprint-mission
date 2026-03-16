package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequestDto;
import com.sprint.mission.discodeit.dto.user.UserResponseDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequestDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepo;
    private final UserStatusRepository userStatusRepo;
    private final BinaryContentRepository binaryContentRepo;

    @Override
    public UserResponseDto create(UserCreateRequestDto dto) {
        if(userRepo.existsByName(dto.name())) throw new DuplicateNameException(dto.name());
        if(userRepo.existsByEmail(dto.email())) throw new DuplicateEmailException(dto.email());

        User user = new User(dto.name(), dto.email(), dto.password(), dto.profileId());
        userRepo.save(user);

        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepo.save(userStatus);

        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), userStatus.passed(), userStatus.getId());
    }

    @Override
    public UserResponseDto findById(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        UserStatus userStatus = userStatusRepo.findByUserId(id)
                .orElseThrow(() -> new UserStatusOfUserNotFoundException(id));

        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), userStatus.passed(), userStatus.getId());
    }

    @Override
    public List<UserResponseDto> findAll() {
        List<User> userList = new ArrayList<>(userRepo.findAll());
        List<UserResponseDto> userStatusList = new ArrayList<>();

        for(User user : userList) {
            UserStatus userStatus = userStatusRepo.findByUserId(user.getId())
                    .orElseThrow(() -> new UserStatusOfUserNotFoundException(user.getId()));
            userStatusList.add(new UserResponseDto(user.getId(), user.getName(), user.getEmail(), userStatus.passed(), userStatus.getId()));
        }
        return userStatusList;
    }

    @Override
    public void update(UserUpdateRequestDto dto) {
        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        if(!user.getName().equals(dto.name()) && userRepo.existsByName(dto.name())) throw new DuplicateNameException(dto.name());
        if(!user.getEmail().equals(dto.email()) && userRepo.existsByEmail(dto.email())) throw new DuplicateEmailException(dto.email());

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        if(dto.profileId() != null) user.setProfileId(dto.profileId());
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
