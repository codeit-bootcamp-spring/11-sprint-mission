package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
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
@Transactional
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> DiscodeitNotFoundException.user(request.getUserId()));

    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> DiscodeitNotFoundException.channel(request.getChannelId()));

    if (readStatusRepository.findByUser_IdAndChannel_Id(
        request.getUserId(), request.getChannelId()).isPresent()) {
      throw DiscodeitDuplicateException.readStatus(
          request.getUserId(), request.getChannelId());
    }

    Instant lastReadAt = request.getLastReadAt() != null
        ? request.getLastReadAt()
        : Instant.now();

    ReadStatus readStatus = new ReadStatus(user, channel, lastReadAt);
    ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

    return readStatusMapper.toDto(savedReadStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public ReadStatusDto find(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.readStatus(id));

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> DiscodeitNotFoundException.user(userId));

    return readStatusRepository.findAllByUser_Id(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void update(ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(request.getReadStatusId())
        .orElseThrow(() -> DiscodeitNotFoundException.readStatus(request.getReadStatusId()));

    readStatus.updateReadStatus(request.getNewLastReadAt());
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.readStatus(id));

    readStatusRepository.delete(readStatus);
  }
}