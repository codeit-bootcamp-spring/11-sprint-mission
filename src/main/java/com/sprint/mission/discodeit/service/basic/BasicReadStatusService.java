package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest dto) {
    // 관련된 Channel, User가 존재하지 않으면 예외를 발생.
    User user = userRepository.findById(dto.userId()).orElseThrow(
        () -> new UserNotFoundException(dto.userId())
    );

    Channel channel = channelRepository.findById(dto.channelId()).orElseThrow(
        () -> new ChannelNotFoundException(dto.channelId())
    );

    // 같은 Channel, User와 관련된 객체가 이미 존재하면 예외를 발생
    readStatusRepository.findByUserAndChannel(user, channel)
        .ifPresent(readStatus -> {
              throw new ReadStatusAlreadyExistException(readStatus.getId());
            }
        );

    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
    readStatusRepository.save(readStatus);

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public ReadStatusDto find(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id).orElseThrow(
        () -> new ReadStatusNotFoundException(id)
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
  public ReadStatusDto update(UUID id, ReadStatusUpdateRequest dto) {
    ReadStatus readStatus = readStatusRepository.findById(id).orElseThrow(
        () -> new ReadStatusNotFoundException(id)
    );

    // 보내려는 필드가 null일 경우 해당 필드는 update하지 않음
    readStatus.update(dto.newLastReadAt(), dto.newNotificationEnabled());

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!readStatusRepository.existsById(id)) {
      throw new ReadStatusNotFoundException(id);
    }
    readStatusRepository.deleteById(id);
  }
}
