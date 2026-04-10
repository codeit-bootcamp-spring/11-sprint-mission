package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NAME_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NAME_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NO_VALID_PARTICIPANTS;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_PARTICIPANTS_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_PRIVATE_UPDATE_FORBIDDEN;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final ChannelMapper mapper;

  @Transactional
  @Override
  public ChannelResponse createPublicChannel(
      PublicChannelCreateRequest publicChannelCreateRequest) {
    if (publicChannelCreateRequest.name() == null || publicChannelCreateRequest.name()
        .isBlank()) {
      throw new ApiException(CHANNEL_NAME_REQUIRED);
    }
    if (this.channelRepository.existsByName(publicChannelCreateRequest.name())) {
      throw new ApiException(CHANNEL_NAME_DUPLICATED);
    }

    Channel channel = new Channel(publicChannelCreateRequest.name(),
        publicChannelCreateRequest.description());
    this.channelRepository.save(channel);

    log.info("{} channel has been created successfully. ✅ [ID: {}]", channel.getName(),
        channel.getId());
    return this.mapper.toResponse(channel);
  }

  @Transactional
  @Override
  public ChannelResponse createPrivateChannel(
      PrivateChannelCreateRequest privateChannelCreateRequest) {
    if (privateChannelCreateRequest.participantIds() == null
        || privateChannelCreateRequest.participantIds().isEmpty()) {
      throw new ApiException(CHANNEL_PARTICIPANTS_REQUIRED);
    }

    Channel channel = new Channel();
    this.channelRepository.save(channel);

    List<UUID> requestedIds = privateChannelCreateRequest.participantIds().stream()
        .distinct()
        .toList();

    List<User> participants = this.userRepository.findAllById(requestedIds).stream()
        .toList();

    if (participants.isEmpty()) {
      throw new ApiException(CHANNEL_NO_VALID_PARTICIPANTS);
    }

    List<ReadStatus> readStatuses = participants.stream()
        .map(user -> new ReadStatus(user, channel, channel.getCreatedAt()))
        .toList();

    this.readStatusRepository.saveAll(readStatuses);

    log.info("private channel has been created successfully. ✅ [ID: {}]", channel.getId());
    return this.mapper.toResponse(channel);
  }

  @Override
  public ChannelResponse findById(UUID id) {
    return this.mapper.toResponse(this.channelRepository.findById(id)
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND))
    );
  }

  @Override
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    Set<Channel> joinedPrivateChannels = this.readStatusRepository.findAllByUserId(userId)
        .stream()
        .map(ReadStatus::getChannel)
        .collect(Collectors.toSet());

    return this.channelRepository.findAll().stream()
        .filter(channel ->
            !channel.isPrivate() || joinedPrivateChannels.contains(channel))
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public ChannelResponse updateChannel(UUID id,
      PublicChannelUpdateRequest publicChannelUpdateRequest) {
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));
    if (channel.isPrivate()) {
      throw new ApiException(CHANNEL_PRIVATE_UPDATE_FORBIDDEN);
    }
    if (publicChannelUpdateRequest.newName() != null && !publicChannelUpdateRequest.newName()
        .isBlank()) {
      if (!channel.getName().equals(publicChannelUpdateRequest.newName())
          && this.channelRepository.existsByName(publicChannelUpdateRequest.newName())) {
        throw new ApiException(CHANNEL_NAME_DUPLICATED);
      }
      channel.updateName(publicChannelUpdateRequest.newName());
    }

    if (publicChannelUpdateRequest.newDescription() != null) {
      channel.updateDescription(publicChannelUpdateRequest.newDescription());
    }

    log.info("{} channel has been updated successfully. ✅ [ID: {}]", channel.getName(),
        channel.getId());
    return this.mapper.toResponse(channel);
  }

  @Transactional
  @Override
  public void deleteChannel(UUID id) {
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));

    this.messageRepository.deleteAllByChannel(channel);

    this.readStatusRepository.deleteAllByChannel(channel);

    this.channelRepository.delete(channel);

    log.info("{} channel has been deleted successfully. ✅ [ID: {}]", channel.getName(), id);
  }
}
