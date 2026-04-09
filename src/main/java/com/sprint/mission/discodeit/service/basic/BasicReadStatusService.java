package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateDto;
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
  public ReadStatus createReadStatus(ReadStatusCreateDto statusCreateDto) {
    User user = userRepository.findById(statusCreateDto.userId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    Channel channel = channelRepository.findById(statusCreateDto.channelId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    readStatusRepository.findByUserIdAndChannelId(statusCreateDto.userId(),
            statusCreateDto.channelId())
        .ifPresent(rs -> {
          throw new BusinessLogicException(ExceptionCode.READ_STATUS_EXISTS);
        });

    ReadStatus readStatus = new ReadStatus(user, channel, Instant.MIN);

    readStatusRepository.save(readStatus);

    return readStatus;
  }

  public ReadStatus findById(UUID id) {
    return readStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.READ_STATUS_NOT_FOUND));
  }

  @Override
  public List<ReadStatus> findAllByUserId(UUID authorId) {
    return readStatusRepository.findAllByUserId(authorId);
  }

  @Override
  @Transactional
  public void deleteReadStatus(UUID id) {
    ReadStatus status = findById(id);

    readStatusRepository.delete(status);
  }

  @Override
  @Transactional
  public ReadStatus updateStatus(UUID readStatusId, ReadStatusUpdateDto request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.READ_STATUS_NOT_FOUND));

    readStatus.update(request.newLastReadAt());

    return readStatus;
  }
}
