package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.READ_STATUS_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.READ_STATUS_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_NOT_FOUND;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ApiException;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Transactional
  @Override
  public ReadStatusResponse createReadStatus(ReadStatusCreateRequest readStatusCreateRequest) {
    User user = this.userRepository.findById(readStatusCreateRequest.userId())
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));
    Channel channel = this.channelRepository.findById(readStatusCreateRequest.channelId())
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));

    if (this.readStatusRepository.existsByUserAndChannel(user, channel)) {
      throw new ApiException(READ_STATUS_DUPLICATED);
    }

    ReadStatus readStatus = new ReadStatus(user, channel, channel.getCreatedAt());
    this.readStatusRepository.save(readStatus);

    log.info("read status has been created successfully. ✅ [ID: {}]", readStatus.getId());
    log.info("-> {user: {}, channel: {}}", user.getId(), channel.getId());
    return this.toResponse(readStatus);
  }

  @Override
  public ReadStatusResponse findById(UUID id) {
    return this.toResponse(this.readStatusRepository.findById(id)
        .orElseThrow(() -> new ApiException(READ_STATUS_NOT_FOUND)));
  }

  @Override
  public List<ReadStatusResponse> findAllByUserId(UUID userId) {
    return this.readStatusRepository.findAllByUserId(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public ReadStatusResponse updateReadStatus(UUID id) {
    ReadStatus readStatus = this.readStatusRepository.findById(id)
        .orElseThrow(() -> new ApiException(READ_STATUS_NOT_FOUND));

    readStatus.updateLastReadAt(Instant.now());

    log.info("read status has been updated successfully. ✅ [ID: {}]", readStatus.getId());
    return this.toResponse(readStatus);
  }

  @Transactional
  @Override
  public void deleteReadStatus(UUID id) {
    ReadStatus readStatus = this.readStatusRepository.findById(id)
        .orElseThrow(() -> new ApiException(READ_STATUS_NOT_FOUND));

    this.readStatusRepository.delete(readStatus);

    log.info("read status has been deleted successfully. ✅ [ID: {}]", readStatus.getId());
  }

  private ReadStatusResponse toResponse(ReadStatus readStatus) {
    return new ReadStatusResponse(
        readStatus.getId(),
        readStatus.getUser().getId(),
        readStatus.getChannel().getId(),
        readStatus.getLastReadAt()
    );
  }
}
