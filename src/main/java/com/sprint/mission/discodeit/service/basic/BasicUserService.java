package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.service.AlreadyExistException;
import com.sprint.mission.discodeit.exception.service.DupEmailException;
import com.sprint.mission.discodeit.exception.service.DupNameException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final ReadStatusRepository readStatusRepository;
  private final ChannelRepository channelRepository;


  @Override
  public CreatedUserDto create(CreateUserDto createUserDTO, MultipartFile file) {

    //유저 생성
    User user = new User(
        createUserDTO.username(),
        createUserDTO.email(),
        createUserDTO.password(),
        null
    );

    //프로필 생성

    BinaryContent content = null;
    if (file != null && !file.isEmpty()) {

      try {
        content = new BinaryContent(
            user.getId(),
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes(),
            file.getSize()
        );
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      content = binaryContentRepository.saveBinaryContent(content);
      user.updateProfileImage(content.getId());

    }

    // 닉네임 체크
    if (userRepository.isExistUserByNickname(createUserDTO.username())) {
      throw new DupNameException();
    }

    //이메일 체크
    if (userRepository.isExistUserByEmail(createUserDTO.email())) {
      throw new DupEmailException();
    }

    //유저 스테이터스 중복 체크
    if (userStatusRepository.isExistUserStatus(user.getId())) {
      throw new AlreadyExistException("이미 존재하는 유저 상태입니다.");
    }

    //유저 저장
    userRepository.saveUser(user);

    //유저 상태 저장
    userStatusRepository.saveUserStatus(new UserStatus(user.getId()));

    //프로필 저장
    if (content != null) {
      binaryContentRepository.saveBinaryContent(content);
    }

    //공개 채널에 대한 ReadStatus 생성
    channelRepository.getAllChannel().forEach(channel -> {

      if (channel.getChannelType() == ChannelType.PUBLIC) {
        readStatusRepository.save(
            new ReadStatus(user.getId(), channel.getId(), Instant.now().minusSeconds(1)));
      }

    });

    return userToInfoDto(user);
  }

  @Override
  public CreatedUserDto find(UUID userId) {

    //유저 가져오기
    User user = userRepository.getUser(userId).orElseThrow();
    return userToInfoDto(user);

  }

  @Override
  public List<UserInfoDto> findAll() {

    //유저 리스트 가져오기
    return userRepository.getAllUser().stream()
        .map(this::userToDto)
        .toList();

  }

  @Override
  public CreatedUserDto updateUser(UUID userId, UpdateUserDto updateUserDto, MultipartFile file) {

    //유저 가져오기
    User user = userRepository.getUser(userId)
        .orElseThrow(() -> new NonExistException("존재 하지 않는 유저 아이디 입니다."));

    //null이면 무시, 있으면 기존 닉네임과 다르면 중복 체크 후 변경
    if (updateUserDto.newUsername() != null && !user.getNickname()
        .equals(updateUserDto.newUsername())) {

      if (userRepository.isExistUserByNickname(updateUserDto.newUsername())) {
        throw new DupNameException();
      }
      user.updateNickname(updateUserDto.newUsername());
    }

    if (updateUserDto.newEmail() != null && !user.getEmail().equals(updateUserDto.newEmail())) {
      if (userRepository.isExistUserByEmail(updateUserDto.newEmail())) {
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
      UUID oldFileId = user.getProfileId();
      BinaryContent newContent;

      try {
        newContent = new BinaryContent(

            userId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes(),
            file.getSize()

        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      binaryContentRepository.saveBinaryContent(newContent);
      user.updateProfileImage(newContent.getId());

      if (oldFileId != null) {
        binaryContentRepository.deleteBinaryContent(oldFileId);
      }
    }

    userRepository.saveUser(user);
    return userToInfoDto(user);
  }


  @Override
  public boolean delete(UUID userId) {

    // 유저 존재 체크
    if (!userRepository.isExistUser(userId)) {
      throw new NonExistException("존재하지 않는 유저 아이디 입니다.");
    }

//    //비밀번호 체크 -> api 변경으로 인해 삭제
//    if (!user.checkSamePassword(user.password())) {
//      throw new DiffPasswordException();
//    }

    //삭제
    userRepository.deleteUser(userId);

    userStatusRepository.deleteUserStatus(userId);

    return true;
  }


  UserInfoDto userToDto(User user) {

    BinaryContent content = binaryContentRepository.getProfileContentByUserId(user.getId())
        .orElse(null);

    UUID profileId = content != null ? content.getId() : null;

    return new UserInfoDto(

        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getNickname(),
        user.getEmail(),
        profileId,
        userStatusRepository.getUserStatus(user.getId()).orElseThrow().isOnline()

    );


  }


  //유저 -> infoDto
  CreatedUserDto userToInfoDto(User user) {

    BinaryContent content = binaryContentRepository.getProfileContentByUserId(user.getId())
        .orElse(null);
    UUID profileId = content != null ? content.getId() : null;

    return new CreatedUserDto(

        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getNickname(),
        user.getEmail(),
        user.getPassword(),
        profileId
    );
  }


}


