package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontentdto.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.dto.userdto.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userdto.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.exception.service.user.DupEmailException;
import com.sprint.mission.discodeit.exception.service.user.DupNameException;
import com.sprint.mission.discodeit.exception.service.user.NonExistUserException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final JPAUserRepository userRepository;
  private final JPABinaryContentRepository binaryContentRepository;
  private final JPAReadStatusRepository readStatusRepository;
  private final JPAChannelRepository channelRepository;
  private final JwtRegistry jwtRegistry;

  private final ApplicationEventPublisher eventPublisher;

  private final UserMapper userMapper;

  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  @CacheEvict(cacheNames = "users", allEntries = true)
  public UserDto create(UserCreateRequest userCreateRequest, MultipartFile file) {

    log.info("유저 생성 요청 : {}", userCreateRequest);

    // 닉네임 중복 체크
    if (userRepository.existsByUsername(userCreateRequest.username())) {
      throw new DupNameException(userCreateRequest.username());
    }

    //이메일 중복 체크
    if (userRepository.existsByEmail(userCreateRequest.email())) {
      throw new DupEmailException(userCreateRequest.email());
    }

    //비밀번호 인코딩
    String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

    //유저 생성
    User user = new User(
        userCreateRequest.username(),
        userCreateRequest.email(),
        encodedPassword,
        null
    );

    //프로필 생성

    BinaryContent content;

    if (file != null && !file.isEmpty()) {
      log.info("프로필 생성 시작");
      try {
        content = new BinaryContent(

            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize()
        );
        eventPublisher.publishEvent(
            new BinaryContentCreatedEvent(content.getId(), content, file.getBytes())
        );
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      user.updateProfile(content);
      log.info("프로필 생성 완료, content : {}", content);
    } else {
      user.updateProfile(null);
    }

    //유저 저장
    userRepository.save(user);

    //공개 채널에 대한 ReadStatus 생성
    channelRepository.findAll().forEach(channel -> {

      if (channel.getType() == ChannelType.PUBLIC) {
        readStatusRepository.save(
            new ReadStatus(user, channel, Instant.now().minusSeconds(1), false));
      }

    });

    log.info("유저 생성 완료 - user : {}", user);
    return userMapper.toDto(user);
  }


  @Override
  @Transactional(readOnly = true)
  public UserDto find(UUID userId) {

    //유저 가져오기
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NonExistUserException(userId));
    return userMapper.toDto(user);

  }

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "users")
  @Override
  public List<UserDto> findAll() {

    //유저 리스트 가져오기
    return userRepository.findAll().stream()
        .map(userMapper::toDto)
        .toList();

  }

  //유저 업데이트

  @Override
  @Transactional
  @PreAuthorize("#userId == principal.userDto.id")
  @CacheEvict(cacheNames = "users", allEntries = true)
  public UserDto updateUser(UUID userId, UserUpdateRequest userUpdateRequest, MultipartFile file) {
    log.info("유저 업데이트 요청, userId : {}, updateUserDto : {}", userId, userUpdateRequest);

    //유저 가져오기
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NonExistUserException(userId));

    //null이면 무시, 있으면 기존 닉네임과 다르면 중복 체크 후 변경
    if (userUpdateRequest.newUsername() != null && !user.getUsername()
        .equals(userUpdateRequest.newUsername())) {

      if (userRepository.existsByUsername(userUpdateRequest.newUsername())) {
        throw new DupNameException(userUpdateRequest.newEmail());
      }
      user.updateUsername(userUpdateRequest.newUsername());
    }

    if (userUpdateRequest.newEmail() != null && !user.getEmail()
        .equals(userUpdateRequest.newEmail())) {
      if (userRepository.existsByEmail(userUpdateRequest.newEmail())) {
        throw new DupEmailException(userUpdateRequest.newEmail());
      }
      user.updateEmail(userUpdateRequest.newEmail());
    }

    if (userUpdateRequest.newPassword() != null) {
      user.updatePassword(userUpdateRequest.newPassword());
    }

    //file 처리
    //file이 존재할 경우에만
    if (file != null && !file.isEmpty()) {
      //이전의
      log.info("새 프로필 생성 시작");
      BinaryContent oldFile = user.getProfile();
      BinaryContent newContent;

      try {

        newContent = new BinaryContent(

            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize()
        );

        eventPublisher.publishEvent(
            new BinaryContentCreatedEvent(newContent.getId(), newContent, file.getBytes()));

        log.info("프로필 생성 완료, newContent : {}", newContent);

      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      user.updateProfile(newContent);

      if (oldFile != null) {
        binaryContentRepository.delete(oldFile);
      }
    }

    log.info("유저 업데이트 완료, user : {}", user);
    return userMapper.toDto(user);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public UserDto updateUserRole(UUID userId, Role role) {

    log.info("유저 역할 변경 시작. userId : {}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NonExistUserException(userId));

    Role oldRole = user.getRole();

    user.updateRole(role);

    jwtRegistry.invalidateJwtInformationByUserId(userId);

    //알림 발생 이벤트
    eventPublisher.publishEvent(new RoleUpdatedEvent(

        userId,
        oldRole,
        role
    ));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  @PreAuthorize("#userId == principal.userDto.id")
  @CacheEvict(cacheNames = "users", allEntries = true)
  public boolean delete(UUID userId) {

    log.info("유저 삭제 요청, userId : {}", userId);

    // 유저 존재 체크
    if (!userRepository.existsById(userId)) {
      throw new NonExistUserException(userId);
    }

    //삭제
    userRepository.deleteById(userId);

    log.info("유저 삭제 완료, userId : {}", userId);
    return true;
  }

}


