package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> {
          log.warn("읽음 상태 생성 실패(존재하지 않는 유저) - userId: {}", request.userId());
          return new UserNotFoundException(request.userId());
        });

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> {
          log.warn("읽음 상태 생성 실패(존재하지 않는 채널) - channelId: {}", request.channelId());
          return new ChannelNotFoundException(request.channelId());
        });

    if (readStatusRepository.existsByUserIdAndChannelId(request.userId(), request.channelId())) {
      log.warn("읽음 상태 생성 실패(이미 상태 존재) - userId: {}, channelId: {}",
          request.userId(), request.channelId());
      throw new ReadStatusAlreadyExistsException(request.userId(), request.channelId());
    }
    ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());
    readStatusRepository.save(readStatus);
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto findById(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("읽음 상태 조회 실패(존재하지 않는 정보) - id: {}", id);
          return new ReadStatusNotFoundException(id);
        });
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findByUserId(userId).stream().
        map(readStatusMapper::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ReadStatusDto update(UUID id, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("읽음 상태 수정 실패(존재하지 않는 정보) - id: {}", id);
          return new ReadStatusNotFoundException(id);
        });
    readStatus.update(request.newLastReadAt());
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    readStatusRepository.deleteById(id);
  }
}
