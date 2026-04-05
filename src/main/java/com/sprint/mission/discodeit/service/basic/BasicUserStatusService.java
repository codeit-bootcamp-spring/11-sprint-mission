package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatusdto.CreateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.UpdateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.UserStatusInfoDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.service.AlreadyExistException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor()
public class BasicUserStatusService implements UserStatusService {


  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;


  @Override
  public UserStatusInfoDto create(CreateUserStatusDto createUserStatusDto) {
    UserStatus userStatus = new UserStatus(
        createUserStatusDto.userId()

    );

    //유저 존재 체크
    if (!userRepository.isExistUser(userStatus.getId())) {
      throw new NonExistException("존재하지 않는 유저 아이디 입니다.");
    }

    // 유저 스테이터스 존재 체크
    if (userStatusRepository.isExistUserStatus(userStatus.getId())) {
      throw new AlreadyExistException("이미 존재하는 유저 스테이터스 입니다");
    }

    //저장
    userStatusRepository.saveUserStatus(userStatus);

    return InfoDtoToStatus(userStatus);

  }

  @Override
  public UserStatusInfoDto find(UUID userId) {

    UserStatus userStatus = userStatusRepository.getUserStatus(userId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 유저 아이디 입니다."));

    return InfoDtoToStatus(userStatus);
  }

  @Override
  public List<UserStatusInfoDto> findAll() {

    return userStatusRepository.getAllUserStatus().stream()
        .map(this::InfoDtoToStatus)
        .toList();
  }

  @Override
  public UserStatusInfoDto update(UUID userId, UpdateUserStatusDto updateUserStatusDto) {

    //가져와서
    UserStatus userStatus = userStatusRepository.getUserStatus(userId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 유저 아이디 입니다."));

    //접속시간 초기화
    userStatus.updateLastActiveAt(updateUserStatusDto.newLastActiveAt());
    //저장
    userStatusRepository.saveUserStatus(userStatus);

    return InfoDtoToStatus(userStatus);

  }

  @Override
  public boolean delete(UUID userId) {

    //존재 체크
    if (!userRepository.isExistUser(userId)) {
      throw new NonExistException("존재하지 않는 유저 아이디 입니다.");
    }

    //삭제
    userRepository.deleteUser(userId);

    return true;

  }


  //Status -> InfoDto
  public UserStatusInfoDto InfoDtoToStatus(UserStatus userStatus) {

    return new UserStatusInfoDto(

        userStatus.getId(),
        userStatus.getCreatedAt(),
        userStatus.getUpdatedAt(),
        userStatus.getUserId(),
        userStatus.getLastActiveAt(),
        userStatus.isOnline()
    );

  }
}
