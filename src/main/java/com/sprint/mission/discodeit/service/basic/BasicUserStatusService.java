package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatusdto.CreateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatusdto.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.service.AlreadyExistException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.repository.JPAUserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor()
public class BasicUserStatusService implements UserStatusService {


  private final JPAUserRepository userRepository;
  private final JPAUserStatusRepository userStatusRepository;
  private final UserStatusMapper userStatusMapper;


  @Override
  public UserStatusDto create(CreateUserStatusDto createUserStatusDto) {
    UserStatus userStatus = new UserStatus(
        createUserStatusDto.user(),
        createUserStatusDto.lastActiveAt()

    );

    //유저 존재 체크
    if (!userRepository.existsById(userStatus.getId())) {
      throw new NonExistException("존재하지 않는 유저 아이디 입니다.");
    }

    // 유저 스테이터스 존재 체크
    if (userStatusRepository.existsById(userStatus.getId())) {
      throw new AlreadyExistException("이미 존재하는 유저 스테이터스 입니다");
    }

    //저장
    userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);

  }

  @Override
  public UserStatusDto find(UUID userId) {

    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 유저 아이디 입니다."));

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public List<UserStatusDto> findAll() {

    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  @Override
  public UserStatusDto update(UUID userId, UserStatusUpdateRequest userStatusUpdateRequest) {

    //가져와서
    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 유저 아이디 입니다."));

    //접속시간 초기화
    userStatus.updateLastActiveAt(userStatusUpdateRequest.newLastActiveAt());
    //저장
    userStatusRepository.save(userStatus);

    return userStatusMapper.toDto(userStatus);

  }

  @Override
  public void delete(UUID userId) {

    //존재 체크
    if (!userRepository.existsById(userId)) {
      throw new NonExistException("존재하지 않는 유저 아이디 입니다.");
    }

    UserStatus status = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 유저 스테이터스 입니다"));
    //삭제
    userStatusRepository.delete(status);


  }


}
