package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Channel with id " + request.channelId() + " not found"));
    User user = userRepository.findById(request.userId())
        .orElseThrow(
            () -> new IllegalArgumentException("User with id " + request.userId() + " not found"));

    readStatusRepository.findByUserIdAndChannelId(request.userId(), request.channelId())
        .ifPresent(rs -> {
          throw new IllegalArgumentException(
              "ReadStatus with userId " + request.userId() + " and channelId " + request.channelId()
                  + " already exists");
        });

    ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());
    readStatusRepository.save(readStatus);
    return toDto(readStatus);
  }

  @Override
  public ReadStatusDto findById(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("ReadStatus with id " + id + " not found"));
    return toDto(readStatus);
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUserId(userId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new IllegalArgumentException(
            "ReadStatus with id " + readStatusId + " not found"));
    readStatus.update(request.newLastReadAt());
    return toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    readStatusRepository.deleteById(id);
  }

  private ReadStatusDto toDto(ReadStatus readStatus) {
    return new ReadStatusDto(
        readStatus.getId(),
        readStatus.getUser().getId(),
        readStatus.getChannel().getId(),
        readStatus.getLastReadAt()
    );
  }
}