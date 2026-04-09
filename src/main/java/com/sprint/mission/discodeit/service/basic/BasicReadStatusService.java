package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  @Transactional
  public ReadStatusDto createReadStatus(ReadStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    readStatusRepository.findByUserIdAndChannelId(request.userId(),
            request.channelId())
        .ifPresent(rs -> {
          throw new BusinessLogicException(ExceptionCode.READ_STATUS_EXISTS);
        });

    ReadStatus readStatus = new ReadStatus(user, channel, Instant.MIN);

    readStatusRepository.save(readStatus);

    return ReadStatusDto.from(readStatus);
  }

  public ReadStatusDto findById(UUID id) {
    ReadStatus status = readStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.READ_STATUS_NOT_FOUND));
    return ReadStatusDto.from(status);
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID authorId) {
    return readStatusRepository.findAllByUserId(authorId).stream()
        .map(ReadStatusDto::from)
        .toList();
  }

  @Override
  @Transactional
  public void deleteReadStatus(UUID id) {
    ReadStatus status = readStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.READ_STATUS_NOT_FOUND));

    readStatusRepository.delete(status);
  }

  @Override
  @Transactional
  public ReadStatusDto updateStatus(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.READ_STATUS_NOT_FOUND));

    readStatus.update(request.newLastReadAt());

    return ReadStatusDto.from(readStatus);
  }
}
