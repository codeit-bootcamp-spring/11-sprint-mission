package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NAME_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NO_VALID_PARTICIPANTS;
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
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    if (this.channelRepository.existsByName(publicChannelCreateRequest.name())) {
      throw new ApiException(CHANNEL_NAME_DUPLICATED);
    }

    Channel channel = new Channel(publicChannelCreateRequest.name(),
        publicChannelCreateRequest.description());
    this.channelRepository.save(channel);

    log.info("{} channel has been created successfully. ✅ [ID: {}]", channel.getName(),
        channel.getId());
    return this.mapper.toResponse(channel, List.of(), Instant.now());
  }

  @Transactional
  @Override
  public ChannelResponse createPrivateChannel(
      PrivateChannelCreateRequest privateChannelCreateRequest) {
    List<UUID> requestedIds = privateChannelCreateRequest.participantIds().stream()
        .distinct()
        .toList();

    List<User> participants = this.userRepository.findAllById(requestedIds).stream()
        .toList();

    if (participants.isEmpty()) {
      throw new ApiException(CHANNEL_NO_VALID_PARTICIPANTS);
    }

    Channel channel = new Channel();
    this.channelRepository.save(channel);

    List<ReadStatus> readStatuses = participants.stream()
        .map(user -> new ReadStatus(user, channel, Instant.now()))
        .toList();

    this.readStatusRepository.saveAll(readStatuses);

    log.info("private channel has been created successfully. ✅ [ID: {}]", channel.getId());
    return this.mapper.toResponse(channel, participants, null);
  }

  @Override
  public ChannelResponse findById(UUID id) {
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));
    List<User> participants = this.readStatusRepository.findAllByChannel(channel).stream()
        .map(ReadStatus::getUser)
        .toList();
    Instant lastMessageAt = this.messageRepository
        .findTopCreatedAtByChannelOrderByCreatedAtDesc(channel).orElse(null);
    return this.mapper.toResponse(channel, participants, lastMessageAt);
  }

  @Override
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    List<Channel> channels = this.channelRepository.findAllByUserId(userId);

    List<UUID> channelIds = channels.stream().map(Channel::getId).toList();

    Map<UUID, List<User>> participantsMap = this.readStatusRepository.findAllByChannelIn(channels)
        .stream()
        .collect(Collectors.groupingBy(
            rs -> rs.getChannel().getId(),
            Collectors.mapping(ReadStatus::getUser, Collectors.toList())
        ));

    Map<UUID, Instant> lastMessageAtMap = this.messageRepository
        .findLastMessageAtByChannelIds(channelIds)
        .stream()
        .collect(Collectors.toMap(
            ChannelResponse.LastMessageAt::getChannelId,
            ChannelResponse.LastMessageAt::getLastMessageAt
        ));

    return channels.stream()
        .map(channel -> this.mapper.toResponse(
            channel,
            participantsMap.getOrDefault(channel.getId(), List.of()),
            lastMessageAtMap.get(channel.getId())
        ))
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
    List<User> participants = this.readStatusRepository.findAllByChannel(channel).stream()
        .map(ReadStatus::getUser)
        .toList();
    Instant lastMessageAt = this.messageRepository
        .findTopCreatedAtByChannelOrderByCreatedAtDesc(channel).orElse(null);
    return this.mapper.toResponse(channel, participants, lastMessageAt);
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
