package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements
    com.sprint.mission.discodeit.service.ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;


  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest dto) {
    User user = userRepository.findById(dto.userId())
        .orElseThrow(
            () -> new NoSuchElementException("User with id " + dto.userId() + " does not exist"));
    Channel channel = channelRepository.findById(dto.channelId())
        .orElseThrow(() -> new NoSuchElementException(
            "Channel with id " + dto.channelId() + " does not exist"));

    if (readStatusRepository.existsByUserIdAndChannelId(user.getId(), channel.getId())) {
      throw new IllegalArgumentException(
          "ReadStatus with userId " + user.getId() + " and channelId " + channel.getId()
              + " already exists");
    }

    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
    readStatus = readStatusRepository.save(readStatus);

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto find(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("ReadStatus with id " + id + " not found"));

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new NoSuchElementException("User with id " + userId + " not found");
    }

    return readStatusRepository.findAllByUserId(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  // 채팅방 읽음
  @Override
  @Transactional
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest dto) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new NoSuchElementException(
            "ReadStatus with id " + readStatusId + " not found"));

    readStatus.updateLastReadAt(Instant.now());

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("ReadStatus with id " + id + " not found"));

    readStatusRepository.delete(readStatus);
  }
}
