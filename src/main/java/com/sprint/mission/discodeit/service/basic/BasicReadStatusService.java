package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.message.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
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

  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatusDto createReadStatus(ReadStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

    readStatusRepository.findByUserIdAndChannelId(request.userId(),
            request.channelId())
        .ifPresent(rs -> {
          throw new ReadStatusAlreadyExistsException(request.userId(), request.channelId());
        });

    boolean notificationEnabled = channel.getType() == ChannelType.PRIVATE;
    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now(), notificationEnabled);

    readStatusRepository.save(readStatus);

    return readStatusMapper.toDto(readStatus);
  }

  public ReadStatusDto findById(UUID id) {
    ReadStatus status = readStatusRepository.findById(id)
        .orElseThrow(() -> new ReadStatusNotFoundException(id));
    return readStatusMapper.toDto(status);
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID authorId) {
    return readStatusRepository.findAllByUserId(authorId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void deleteReadStatus(UUID id) {
    ReadStatus status = readStatusRepository.findById(id)
        .orElseThrow(() -> new ReadStatusNotFoundException(id));

    readStatusRepository.delete(status);
  }

  @Override
  @Transactional
  public ReadStatusDto updateStatus(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

    if (request.newLastReadAt() != null) {
      readStatus.update(request.newLastReadAt());
    }
    if (request.notificationEnabled() != null) {
      readStatus.updateNotificationEnabled(request.notificationEnabled());
    }

    return readStatusMapper.toDto(readStatus);
  }
}
