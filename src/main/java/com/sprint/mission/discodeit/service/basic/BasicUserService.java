package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import com.sprint.mission.discodeit.exception.service.DupEmailException;
import com.sprint.mission.discodeit.exception.service.DupNameException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;


import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final JPAUserRepository userRepository;
  private final JPABinaryContentRepository binaryContentRepository;
  private final JPAReadStatusRepository readStatusRepository;
  private final JPAChannelRepository channelRepository;

  private final BinaryContentStorage binaryContentStorage;

  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto create(UserCreateRequest userCreateRequest, MultipartFile file) {

    // 닉네임 중복 체크
    if (userRepository.existsByUsername(userCreateRequest.username())) {
      throw new DupNameException();
    }

    //이메일 중복 체크
    if (userRepository.existsByEmail(userCreateRequest.email())) {
      throw new DupEmailException();
    }

    //유저 생성
    User user = new User(
        userCreateRequest.username(),
        userCreateRequest.email(),
        userCreateRequest.password(),
        null,
        null
    );

    //프로필 생성

    BinaryContent content;
    if (file != null && !file.isEmpty()) {

      try {
        content = new BinaryContent(

            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize()
        );
        binaryContentStorage.put(content.getId(), file.getBytes());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      user.updateProfile(content);
    } else {
      user.updateProfile(null);
    }

    //유저 스테이터스 생성

    UserStatus userStatus = new UserStatus(user, Instant.now());
    user.updateStatus(userStatus);

    //유저 저장
    userRepository.save(user);

    //공개 채널에 대한 ReadStatus 생성
    channelRepository.findAll().forEach(channel -> {

      if (channel.getType() == ChannelType.PUBLIC) {
        readStatusRepository.save(
            new ReadStatus(user, channel, Instant.now().minusSeconds(1)));
      }

    });

    return userMapper.toDto(user);
  }


  @Override
  @Transactional(readOnly = true)
  public UserDto find(UUID userId) {

    //유저 가져오기
    User user = userRepository.findById(userId).orElseThrow();
    return userMapper.toDto(user);

  }

  @Transactional(readOnly = true)
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
  public UserDto updateUser(UUID userId, UpdateUserDto updateUserDto, MultipartFile file) {

    //유저 가져오기
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NonExistException("존재 하지 않는 유저 아이디 입니다."));

    //null이면 무시, 있으면 기존 닉네임과 다르면 중복 체크 후 변경
    if (updateUserDto.newUsername() != null && !user.getUsername()
        .equals(updateUserDto.newUsername())) {

      if (userRepository.existsByUsername(updateUserDto.newUsername())) {
        throw new DupNameException();
      }
      user.updateUsername(updateUserDto.newUsername());
    }

    if (updateUserDto.newEmail() != null && !user.getEmail().equals(updateUserDto.newEmail())) {
      if (userRepository.existsByEmail(updateUserDto.newEmail())) {
        throw new DupEmailException();
      }
      user.updateEmail(updateUserDto.newEmail());
    }

    if (updateUserDto.newPassword() != null) {
      user.updatePassword(updateUserDto.newPassword());
    }

    //file 처리
    //file이 존재할 경우에만
    if (file != null && !file.isEmpty()) {
      //이전의
      BinaryContent oldFile = user.getProfile();
      BinaryContent newContent;

      try {

        newContent = new BinaryContent(

            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize()
        );
        binaryContentStorage.put(newContent.getId(), file.getBytes());

      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      user.updateProfile(newContent);

      if (oldFile != null) {
        binaryContentRepository.delete(oldFile);
      }
    }

    return userMapper.toDto(user);
  }


  @Override
  @Transactional
  public boolean delete(UUID userId) {

    // 유저 존재 체크
    if (!userRepository.existsById(userId)) {
      throw new NonExistException("존재하지 않는 유저 아이디 입니다.");
    }

    //삭제
    userRepository.deleteById(userId);
    return true;
  }

}


