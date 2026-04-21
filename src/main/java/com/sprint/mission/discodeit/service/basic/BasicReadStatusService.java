package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

    if (readStatusRepository.existsByUserIdAndChannelId(request.userId(), request.channelId())) {
      throw new IllegalArgumentException("이미 해당 채널의 읽음 상태 정보가 존재합니다.");
    }
    ReadStatus readStatus = new ReadStatus(user, channel, request.lastReadAt());
    readStatusRepository.save(readStatus);
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto findById(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("정보를 찾을 수 없습니다."));
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
        .orElseThrow(() -> new IllegalArgumentException("정보를 찾을 수 없습니다."));
    readStatus.update(request.newLastReadAt());
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    readStatusRepository.deleteById(id);
  }
}
