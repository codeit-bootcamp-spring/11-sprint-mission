package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
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
@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Transactional
  @Override
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    UUID userId = request.userId();
    UUID channelId = request.channelId();

    User user = userRepository.findById(userId)
        .orElseThrow(() ->{
          log.warn("존재하지 않는 id: {}", userId);
          return new UserNotFoundException();
        });
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() ->{
          log.warn("존재하지 않는 채널: {}", channelId);
          return new ChannelNotFoundException();
        });

    ReadStatus readStatus = readStatusRepository.findByUserIdAndChannelId(user.getId(), channel.getId())
        .orElseGet(() -> {
          Instant lastReadAt = request.lastReadAt();
          return readStatusRepository.save(new ReadStatus(user, channel, lastReadAt));
        });

    log.info("ReadStatus 생성 완료: {}", readStatus.getId());
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto find(UUID readStatusId) {
    return readStatusRepository.findById(readStatusId)
        .map(readStatusMapper::toDto)
        .orElseThrow(() ->{
          log.warn("존재하지 않는 ReadStatusId: {}", readStatusId);
          return new ReadStatusNotFoundException();
        });
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUserId(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> {
          log.warn("존재하지 않는 id: {}", readStatusId);
          return new ReadStatusNotFoundException();
        });
    readStatus.update(request.newLastReadAt());
    log.info("ReadStatusId 업데이트 완료: {}", readStatusId);
    return readStatusMapper.toDto(readStatus);
  }

  @Transactional
  @Override
  public void delete(UUID readStatusId) {
    if (!readStatusRepository.existsById(readStatusId)) {
      log.warn("존재하지 않는 readStatusId: {}", readStatusId);
      throw new ReadStatusNotFoundException();
    }
    readStatusRepository.deleteById(readStatusId);
    log.info("ReadStatus 삭제 완료: {}", readStatusId);
  }
}