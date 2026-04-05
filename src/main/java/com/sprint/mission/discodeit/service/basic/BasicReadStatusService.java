package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    if (userRepository.findById(request.userId()).isEmpty()) {
      throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
    }
    if (channelRepository.findById(request.channelId()) == null) {
      throw new IllegalArgumentException("존재하지 않는 채널입니다.");
    }
    if (readStatusRepository.existsByUserIdAndChannelId(request.userId(), request.channelId())) {
      throw new IllegalArgumentException("이미 해당 채널의 읽음 상태 정보가 존재합니다.");
    }
    ReadStatus readStatus = new ReadStatus(request.userId(), request.channelId(),
        request.lastReadAt());
    readStatusRepository.save(readStatus);
    return toDto(readStatus);
  }

  @Override
  public ReadStatusDto findById(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id);
    if (readStatus == null) {
      throw new IllegalArgumentException("정보를 찾을 수 없습니다.");
    }
    return toDto(readStatus);
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findByUserId(userId).stream().
        map(this::toDto).collect(Collectors.toList());
  }

  @Override
  public ReadStatusDto update(UUID id, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(id);
    if (readStatus == null) {
      throw new IllegalArgumentException("정보를 찾을 수 없습니다.");
    }
    readStatus.update(request.newLastReadAt());
    readStatusRepository.save(readStatus);
    return toDto(readStatus);
  }

  @Override
  public void delete(UUID id) {
    readStatusRepository.delete(id);
  }

  private ReadStatusDto toDto(ReadStatus readStatus) {
    return new ReadStatusDto(readStatus.getId(), readStatus.getUserId(),
        readStatus.getChannelId(), readStatus.getCreatedAt(),
        readStatus.getUpdatedAt(), readStatus.getLastReadAt());
  }
}
