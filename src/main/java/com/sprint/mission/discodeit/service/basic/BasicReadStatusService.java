package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.DuplicateReadStatusException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper mapper;

  @Transactional
  @Override
  public ReadStatusResponse createReadStatus(ReadStatusCreateRequest readStatusCreateRequest) {
    log.debug("read-status create trial: {}", readStatusCreateRequest);
    User user = this.userRepository.findById(readStatusCreateRequest.userId())
        .orElseThrow(() -> UserNotFoundException.withId(readStatusCreateRequest.userId()));
    Channel channel = this.channelRepository.findById(readStatusCreateRequest.channelId())
        .orElseThrow(() -> ChannelNotFoundException.withId(readStatusCreateRequest.channelId()));

    if (this.readStatusRepository.existsByUserAndChannel(user, channel)) {
      throw DuplicateReadStatusException.withUserAndChannel(user.getId(), channel.getId());
    }

    ReadStatus readStatus = new ReadStatus(user, channel, readStatusCreateRequest.lastReadAt(),
        false);
    this.readStatusRepository.save(readStatus);

    log.info("read-status create success: id={}, userId={}, channelId={}",
        readStatus.getId(), user.getId(), channel.getId());
    return this.mapper.toResponse(readStatus);
  }

  @Override
  public ReadStatusResponse findById(UUID id) {
    log.debug("read-status find-by-id trial: id={}", id);
    ReadStatus readStatus = this.readStatusRepository.findById(id)
        .orElseThrow(() -> ReadStatusNotFoundException.withId(id));

    log.info("read-status find-by-id success: id={}", id);
    return this.mapper.toResponse(readStatus);
  }

  @Override
  public List<ReadStatusResponse> findAllByUserId(UUID userId) {
    log.debug("read-status find-all-by-user-id trial: userId={}", userId);
    List<ReadStatus> readStatuses = this.readStatusRepository.findAllByUserId(userId);

    log.info("read-status find-all-by-user-id success: userId={}, count={}",
        userId, readStatuses.size());
    return readStatuses.stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public ReadStatusResponse updateReadStatus(UUID id,
      ReadStatusUpdateRequest readStatusUpdateRequest) {
    log.debug("read-status update trial: id={}, request={}", id, readStatusUpdateRequest);
    ReadStatus readStatus = this.readStatusRepository.findById(id)
        .orElseThrow(() -> ReadStatusNotFoundException.withId(id));

    if (readStatusUpdateRequest.newLastReadAt() != null) {
      readStatus.updateLastReadAt(readStatusUpdateRequest.newLastReadAt());
    }
    if (readStatusUpdateRequest.newNotificationEnabled() != null) {
      readStatus.updateNotificationEnabled(readStatusUpdateRequest.newNotificationEnabled());
    }

    log.info("read-status update success: id={}", readStatus.getId());
    return this.mapper.toResponse(readStatus);
  }

  @Transactional
  @Override
  public void deleteReadStatus(UUID id) {
    log.debug("read-status delete trial: id={}", id);
    ReadStatus readStatus = this.readStatusRepository.findById(id)
        .orElseThrow(() -> ReadStatusNotFoundException.withId(id));

    this.readStatusRepository.delete(readStatus);

    log.info("read-status delete success: id={}", readStatus.getId());
  }
}