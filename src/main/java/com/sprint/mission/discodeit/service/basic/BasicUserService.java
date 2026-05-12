package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final BinaryContentService binaryContentService;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest dto, MultipartFile profile) {
        if(userRepo.findByUsername(dto.username()).isPresent()) throw new UsernameAlreadyExistsException(dto.username());
        if(userRepo.findByEmail(dto.email()).isPresent()) throw new EmailAlreadyExistsException(dto.email());

        BinaryContent binaryContent = binaryContentService.create(profile);

        User user = new User(dto.username(), dto.email(), dto.password(), binaryContent);
        new UserStatus(user, Instant.now());
        userRepo.save(user);
        log.info("User created. userId={}, name={}", user.getId(), user.getUsername());

        return userMapper.toDto(user);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepo.findWithStatusAndProfileById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepo.findAllWithStatusAndProfile().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void update(UUID id, UserUpdateRequest dto, MultipartFile profile) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if(!user.getUsername().equals(dto.newUsername())
                && userRepo.findByUsername(dto.newUsername()).isPresent())
            throw new UsernameAlreadyExistsException(dto.newUsername());
        if(!user.getEmail().equals(dto.newEmail())
                && userRepo.findByEmail(dto.newEmail()).isPresent())
            throw new EmailAlreadyExistsException(dto.newEmail());

        BinaryContent oldProfile = user.getProfile();
        BinaryContent newProfile = binaryContentService.create(profile);
        BinaryContent updateProfile = newProfile != null ? newProfile : oldProfile;

        user.update(dto.newUsername(), dto.newEmail(), dto.newPassword(), updateProfile);
        log.info("User updated. userId={}", user.getId());

        if(newProfile != null && oldProfile != null) {
            binaryContentService.delete(oldProfile);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        BinaryContent profile = user.getProfile();

        if(profile != null) binaryContentService.delete(profile);

        userRepo.delete(user);
        log.info("User deleted. userId={}", user.getId());
    }
}
