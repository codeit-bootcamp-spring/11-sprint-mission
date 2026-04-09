package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest dto, MultipartFile profile) {
        if(userRepo.findByUsername(dto.username()).isPresent()) throw new BusinessException(ErrorCode.DUPLICATE_NAME);
        if(userRepo.findByEmail(dto.email()).isPresent()) throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);

        BinaryContent binaryContent = saveProfile(profile);

        User user = new User(dto.username(), dto.email(), dto.password(), binaryContent);
        new UserStatus(user, Instant.now());
        userRepo.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepo.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void update(UUID id, UserUpdateRequest dto, MultipartFile profile) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(!user.getUsername().equals(dto.newUsername())
                && userRepo.findByUsername(dto.newUsername()).isPresent())
            throw new BusinessException(ErrorCode.DUPLICATE_NAME);
        if(!user.getEmail().equals(dto.newEmail())
                && userRepo.findByEmail(dto.newEmail()).isPresent())
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);

        BinaryContent oldProfile = user.getProfile();
        BinaryContent newProfile = saveProfile(profile);
        BinaryContent updateProfile = newProfile != null ? newProfile : oldProfile;

        user.update(dto.newUsername(), dto.newEmail(), dto.newPassword(), updateProfile);

        if(newProfile != null && oldProfile != null) {
            binaryContentRepo.deleteById(oldProfile.getId());
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        BinaryContent profile = user.getProfile();

        if(profile != null) {
            binaryContentRepo.deleteById(profile.getId());
        }

        userRepo.delete(user);
    }

    private BinaryContent saveProfile(MultipartFile file) {
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
            return binaryContent;
        } catch (IOException e){
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }
}
