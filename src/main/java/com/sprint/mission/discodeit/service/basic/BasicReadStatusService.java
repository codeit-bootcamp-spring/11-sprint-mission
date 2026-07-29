package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final ReadStatusMapper readStatusMapper;

  //create
  @Transactional
  @Override
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    // 유저 검증
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    //채널 검증
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

    // 같은 채널의 스테이터스가 이미 존재하면 예외
    readStatusRepository.findByUser_IdAndChannel_Id(request.userId(), request.channelId())
        .ifPresent(rs -> {
          throw new ReadStatusAlreadyExistsException(request.userId(), request.channelId());
        });

    ReadStatus readStatus = new ReadStatus(user, channel);

    return readStatusMapper.toDto(readStatusRepository.save(readStatus));
  }

  //Read
  @Override
  @Transactional(readOnly = true)
  public ReadStatusDto findById(UUID readStatusId) {
    return readStatusMapper.toDto(findReadStatusOrThrow(readStatusId));
  }

  //Read all
  @Override
  @Transactional(readOnly = true)
  public List<ReadStatusDto> findAll() {
    return readStatusRepository.findAll().stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUser_Id(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  //Update
  @Transactional
  @Override
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = findReadStatusOrThrow(readStatusId);
    if (request.newLastReadAt() != null) {
      readStatus.updateLastReadAt(request.newLastReadAt());
    }
    readStatus.updateNotificationEnabled(request.newNotificationEnabled());
    return readStatusMapper.toDto(readStatus);
  }

  //Delete
  @Transactional
  @Override
  public void delete(UUID readStatusId) {
    findReadStatusOrThrow(readStatusId);
    //스테이터스 삭제
    readStatusRepository.deleteById(readStatusId);
  }

  // read 스테이터스 검증 로직
  private ReadStatus findReadStatusOrThrow(UUID readStatusId) {
    return readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));
  }
}
