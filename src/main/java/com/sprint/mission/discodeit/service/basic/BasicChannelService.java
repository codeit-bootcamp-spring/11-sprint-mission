package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.DuplicateChannelException;
import com.sprint.mission.discodeit.exception.channel.NoValidParticipantsException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateForbiddenException;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
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

  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @Transactional
  @Override
  public ChannelResponse createPublicChannel(
      PublicChannelCreateRequest publicChannelCreateRequest) {
    log.debug("channel create-public trial: {}", publicChannelCreateRequest);
    if (this.channelRepository.existsByName(publicChannelCreateRequest.name())) {
      throw DuplicateChannelException.withName(publicChannelCreateRequest.name());
    }

    Channel channel = new Channel(publicChannelCreateRequest.name(),
        publicChannelCreateRequest.description());
    this.channelRepository.save(channel);

    log.info("channel create-public success: id={}, name={}", channel.getId(), channel.getName());
    return this.mapper.toResponse(channel, List.of(), Instant.now());
  }


  @Transactional
  @Override
  public ChannelResponse createPrivateChannel(
      PrivateChannelCreateRequest privateChannelCreateRequest) {
    log.debug("channel create-private trial: {}", privateChannelCreateRequest);
    List<UUID> requestedIds = privateChannelCreateRequest.participantIds().stream()
        .distinct()
        .toList();

    List<User> participants = this.userRepository.findAllById(requestedIds).stream()
        .toList();

    if (participants.isEmpty()) {
      throw NoValidParticipantsException.withRequestedIds(requestedIds);
    }

    Channel channel = new Channel();
    this.channelRepository.save(channel);

    List<ReadStatus> readStatuses = participants.stream()
        .map(user -> new ReadStatus(user, channel, Instant.now(), true))
        .toList();

    this.readStatusRepository.saveAll(readStatuses);

    log.info("channel create-private success: id={}, participants-count={}", channel.getId(),
        participants.size());
    return this.mapper.toResponse(channel, participants, null);
  }

  @Override
  public ChannelResponse findById(UUID id) {
    log.debug("channel find-by-id trial: id={}", id);
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> ChannelNotFoundException.withId(id));
    List<User> participants = this.readStatusRepository.findAllByChannel(channel).stream()
        .map(ReadStatus::getUser)
        .toList();
    Instant lastMessageAt = this.messageRepository
        .findTopCreatedAtByChannelOrderByCreatedAtDesc(channel).orElse(null);

    log.info(
        "channel find-by-id success: id={}, name={}, participants-count={}, last-message-at={}",
        channel.getId(), channel.getName(), participants.size(), lastMessageAt);
    return this.mapper.toResponse(channel, participants, lastMessageAt);
  }

  @Cacheable(cacheNames = "channels", key = "#userId")
  @Override
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    log.debug("channel find-all-by-user-id trial: userId={}", userId);
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

    log.info("channel find-all-by-user-id success: count={}", channels.size());
    return channels.stream()
        .map(channel -> this.mapper.toResponse(
            channel,
            participantsMap.getOrDefault(channel.getId(), List.of()),
            lastMessageAtMap.get(channel.getId())
        ))
        .toList();
  }

  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @Transactional
  @Override
  public ChannelResponse updateChannel(UUID id,
      PublicChannelUpdateRequest publicChannelUpdateRequest) {
    log.debug("channel update trial: id={}, request={}", id, publicChannelUpdateRequest);
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> ChannelNotFoundException.withId(id));
    if (channel.isPrivate()) {
      throw PrivateChannelUpdateForbiddenException.withId(id);
    }
    if (publicChannelUpdateRequest.newName() != null && !publicChannelUpdateRequest.newName()
        .isBlank()) {
      if (!channel.getName().equals(publicChannelUpdateRequest.newName())
          && this.channelRepository.existsByName(publicChannelUpdateRequest.newName())) {
        throw DuplicateChannelException.withName(publicChannelUpdateRequest.newName());
      }
      channel.updateName(publicChannelUpdateRequest.newName());
    }

    if (publicChannelUpdateRequest.newDescription() != null) {
      channel.updateDescription(publicChannelUpdateRequest.newDescription());
    }

    log.info("channel update success: id={}, name={}", channel.getId(), channel.getName());
    List<User> participants = this.readStatusRepository.findAllByChannel(channel).stream()
        .map(ReadStatus::getUser)
        .toList();
    Instant lastMessageAt = this.messageRepository
        .findTopCreatedAtByChannelOrderByCreatedAtDesc(channel).orElse(null);
    return this.mapper.toResponse(channel, participants, lastMessageAt);
  }

  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @Transactional
  @Override
  public void deleteChannel(UUID id) {
    log.debug("channel delete trial: id={}", id);
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> ChannelNotFoundException.withId(id));

    this.messageRepository.deleteAllByChannel(channel);

    this.readStatusRepository.deleteAllByChannel(channel);

    this.channelRepository.delete(channel);

    log.info("channel delete success: id={}", id);
  }
}