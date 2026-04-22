package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
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
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;


  @Override
  @Transactional
  public ReadStatusDto.Response create(ReadStatusDto.CreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    // 같은 Channel 및 User 관련 객체 존재 시
    if (readStatusRepository.existsByUserIdAndChannelId(request.userId(), request.channelId())) {
      throw new BusinessException(ErrorCode.READ_STATUS_ALREADY_EXISTS);
    }
    ReadStatus readStatus = request.toEntity(user, channel);
    readStatusRepository.save(readStatus);

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto.Response findById(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public List<ReadStatusDto.Response> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUserIdWithChannelAndUser(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ReadStatusDto.Response update(UUID id, ReadStatusDto.UpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND));

    Instant newLastReadAt = (request != null && request.newLastReadAt() != null)
        ? request.newLastReadAt()
        : Instant.now();
    readStatus.update(newLastReadAt);

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!readStatusRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.READ_STATUS_NOT_FOUND);
    }
    readStatusRepository.deleteById(id);
  }
}