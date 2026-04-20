package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatus create(ReadStatusCreateRequest dto) {
    // 관련된 Channel, User가 존재하지 않으면 예외를 발생.
    User user = userRepository.findById(dto.userId()).orElseThrow(
        () -> new NoSuchElementException("해당 User가 존재하지 않습니다. UserId :" + dto.userId()));

    Channel channel = channelRepository.findById(dto.channelId()).orElseThrow(
        () -> new NoSuchElementException("해당 Channel이 존재하지 않습니다. ChannelId : " + dto.channelId()));

    // 같은 Channel, User와 관련된 객체가 이미 존재하면 예외를 발생
    readStatusRepository.findByUserAndChannel(user, channel)
        .ifPresent(readStatus -> {
              throw new IllegalArgumentException(
                  "이미 해당 채널의 읽음 상태가 존재합니다. user: " + dto.userId() +
                      ", channelId: " + dto.channelId());
            }
        );

    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
    readStatusRepository.save(readStatus);

    return readStatus;
  }

  @Override
  @Transactional(readOnly = true)
  public ReadStatusDto find(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 ReadStatus입니다. id : " + id)
    );

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReadStatusDto> findAllByUserId(List<UUID> userIds) {
    return readStatusRepository.findAllByUserIdIn(userIds).stream()
        .map(readStatusMapper::toDto).toList();
  }

  @Override
  @Transactional
  public ReadStatus update(UUID id, ReadStatusUpdateRequest dto) {
    ReadStatus readStatus = readStatusRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("해당 ReadStatus가 존재하지 않습니다. ReadStatus Id : " + id));
    readStatus.updateLastReadAt(dto.updatelastReadAt());
    readStatusRepository.save(readStatus);

    return readStatus;
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!readStatusRepository.existsById(id)) {
      throw new NoSuchElementException("해당 ReadStatus는 존재하지 않습니다. ReadStatus Id : " + id);
    }
    readStatusRepository.deleteById(id);
  }
}
