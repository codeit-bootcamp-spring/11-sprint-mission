package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatusdto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatusdto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.channel.NonExistChannelException;
import com.sprint.mission.discodeit.exception.service.readstatus.DupReadStatus;
import com.sprint.mission.discodeit.exception.service.readstatus.NonExistReadStatusException;
import com.sprint.mission.discodeit.exception.service.user.NonExistUserException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
      throw new DupReadStatus(readStatusCreateRequest.userId(),
          readStatusCreateRequest.channelId());
    }

    //유저, 채널 가져오기
    User user = userRepository.findById(readStatusCreateRequest.userId())
        .orElseThrow(() -> new NonExistUserException(readStatusCreateRequest.userId()));
    Channel channel = channelRepository.findById(readStatusCreateRequest.channelId())
        .orElseThrow(() -> new NonExistChannelException(readStatusCreateRequest.channelId()));

    boolean isChannelPrivate = channel.getType() == Channel.ChannelType.PRIVATE;

    //ReadStatus 생성
    ReadStatus readStatus = new ReadStatus(
        user,
        channel,
        readStatusCreateRequest.lastReadAt(),
        isChannelPrivate

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
        .orElseThrow(() -> new NonExistReadStatusException(readStatusId));
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
        .orElseThrow(() -> new NonExistReadStatusException(readStatusId));

    readStatus.updateLastReadAt(readStatusUpdateRequestDto.newLastReadAt());
    readStatus.updateNotificationEnabled(readStatusUpdateRequestDto.notificationEnabled());

    if (readStatusUpdateRequestDto.newLastReadAt() == null) {
      readStatus.updateLastReadAt(Instant.now().minusSeconds(1));
    }

    return readStatusMapper.toDto(readStatus);

  }

  @Override
  @Transactional
  public void delete(UUID readStatusId) {

    if (!readStatusRepository.existsById(readStatusId)) {
      throw new NonExistReadStatusException(readStatusId);
    }

    readStatusRepository.deleteById(readStatusId);


  }


}
