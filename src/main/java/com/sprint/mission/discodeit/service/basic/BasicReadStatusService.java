package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatusdto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatusdto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.AlreadyExistException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final JPAReadStatusRepository readStatusRepository;
  private final JPAUserRepository userRepository;
  private final JPAChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest readStatusCreateRequest) {

    //존재하는 스테이터스 인지 체크

    if (readStatusRepository.existsByUserIdAndChannelId(readStatusCreateRequest.userId(),
        readStatusCreateRequest.channelId())) {
      throw new AlreadyExistException("이미 존재하는 유저와 채널의 읽기 상태입니다");
    }

    //유저, 채널 가져오기
    User user = userRepository.findById(readStatusCreateRequest.userId())
        .orElseThrow(() -> new NonExistException("존재하지 않는 유저 아이디입니다."));
    Channel channel = channelRepository.findById(readStatusCreateRequest.channelId())
        .orElseThrow(() -> new NonExistException("존재하지 않는 채널 아이디 입니다."));

    //ReadStatus 생성
    ReadStatus readStatus = new ReadStatus(
        user,
        channel,
        readStatusCreateRequest.lastReadAt()
    );

    if (readStatusCreateRequest.lastReadAt() == null) {
      readStatus.updateLastReadAt(Instant.now().minusSeconds(1));
    }

    //저장
    readStatusRepository.save(readStatus);
    //Dto 반환
    return readStatusMapper.toDto(readStatus);

  }

  @Override
  @Transactional(readOnly = true)
  public ReadStatusDto find(UUID readStatusId) {

    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new NonExistException("존재하지 않는 읽기 상태 아이디입니다."));
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReadStatusDto> findAllByUserId(UUID userId) {

    return readStatusRepository.findAllByUserId(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ReadStatusDto update(UUID readStatusId,
      ReadStatusUpdateRequest readStatusUpdateRequestDto) {

    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new NonExistException("존재하는 읽기 상태 아이디가 아닙니다."));

    readStatus.updateLastReadAt(readStatusUpdateRequestDto.newLastReadAt());

    if (readStatusUpdateRequestDto.newLastReadAt() == null) {
      readStatus.updateLastReadAt(Instant.now().minusSeconds(1));
    }

    return readStatusMapper.toDto(readStatus);

  }

  @Override
  @Transactional
  public void delete(UUID readStatusId) {

    if (!readStatusRepository.existsById(readStatusId)) {
      throw new NonExistException("존재하지 않는 읽기 상태입니다.");
    }

    readStatusRepository.deleteById(readStatusId);


  }


}
