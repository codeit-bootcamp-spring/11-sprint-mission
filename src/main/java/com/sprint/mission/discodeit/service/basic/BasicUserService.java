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
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepo;
    private final BinaryContentRepository binaryContentRepo;
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;

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
            binaryContentStorage.deleteById(oldProfile.getId());
            binaryContentRepo.delete(oldProfile);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        BinaryContent profile = user.getProfile();

        if(profile != null) {
            binaryContentStorage.deleteById(profile.getId());
            binaryContentRepo.delete(profile);
        }

        userRepo.delete(user);
    }

    private BinaryContent saveProfile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = file.getBytes();

            BinaryContent binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    (long) bytes.length
            );
            binaryContentRepo.save(binaryContent);
            binaryContentStorage.put(binaryContent.getId(), bytes);

            return binaryContent;
        } catch (IOException e){
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }
}
