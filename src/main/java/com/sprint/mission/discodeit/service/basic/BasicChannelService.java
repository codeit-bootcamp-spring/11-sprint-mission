package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NAME_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NAME_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_NO_VALID_PARTICIPANTS;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_PARTICIPANTS_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.CHANNEL_PRIVATE_UPDATE_FORBIDDEN;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

  @Transactional
  @Override
  public ChannelResponse createPublicChannel(
      PublicChannelCreateRequest publicChannelCreateRequest) {
    if (publicChannelCreateRequest.name() == null || publicChannelCreateRequest.name()
        .isBlank()) {
      throw new ApiException(CHANNEL_NAME_REQUIRED);
    }
    if (this.channelRepository.existByName(publicChannelCreateRequest.name())) {
      throw new ApiException(CHANNEL_NAME_DUPLICATED);
    }

    Channel channel = new Channel(publicChannelCreateRequest.name(),
        publicChannelCreateRequest.description());
    this.channelRepository.save(channel);

    log.info("{} channel has been created successfully. ✅ [ID: {}]", channel.getName(),
        channel.getId());
    return this.toResponse(channel, null, new ArrayList<>());
  }

  @Transactional
  @Override
  public ChannelResponse createPrivateChannel(
      PrivateChannelCreateRequest privateChannelCreateRequest) {
    if (privateChannelCreateRequest.participants() == null
        || privateChannelCreateRequest.participants().isEmpty()) {
      throw new ApiException(CHANNEL_PARTICIPANTS_REQUIRED);
    }

    Channel channel = new Channel();
    this.channelRepository.save(channel);

    List<UUID> requestedIds = privateChannelCreateRequest.participants().stream()
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
    return this.toResponse(channel, null, participants);
  }

  @Override
  public ChannelResponse findById(UUID id) {
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));

    Instant lastMessageAt = this.messageRepository.findTopCreatedAtByChannelOrderByCreatedAtDesc(
            channel)
        .orElse(null);
    List<User> participants = this.readStatusRepository.findAllByChannel(channel).stream()
        .map(ReadStatus::getUser)
        .toList();

    return this.toResponse(channel, lastMessageAt, participants);
  }

  @Override
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    Set<Channel> joinedPrivateChannels = this.readStatusRepository.findAllByUserId(userId)
        .stream()
        .map(ReadStatus::getChannel)
        .collect(Collectors.toSet());

    List<Channel> channels = this.channelRepository.findAll().stream()
        .filter(
            channel -> !channel.isPrivate() || joinedPrivateChannels.contains(
                channel))
        .toList();

    // TODO: 메시지 많아질 경우 단일 쿼리(Projection)로 최적화 고려
    Map<UUID, Instant> lastMessageAtByChannel =
        this.messageRepository.findAllByChannelIn(channels).stream()
            .collect(Collectors.toMap(
                message -> message.getChannel().getId(),
                Message::getCreatedAt,
                (a, b) -> a.isAfter(b) ? a : b
            ));

    Map<UUID, List<User>> participantsByChannel = this.readStatusRepository.findAllByChannelIn(
            channels).stream()
        .collect(Collectors.groupingBy(
            readStatus -> readStatus.getChannel().getId(),
            Collectors.mapping(ReadStatus::getUser, Collectors.toList())
        ));

    return channels.stream()
        .map(channel -> this.toResponse(
            channel,
            lastMessageAtByChannel.getOrDefault(channel.getId(), null),
            participantsByChannel.getOrDefault(channel.getId(), new ArrayList<>())
        ))
        .toList();
  }

  @Transactional
  @Override
  public ChannelResponse updateChannel(UUID id, ChannelUpdateRequest channelUpdateRequest) {
    Channel channel = this.channelRepository.findById(id)
        .orElseThrow(() -> new ApiException(CHANNEL_NOT_FOUND));
    if (channel.isPrivate()) {
      throw new ApiException(CHANNEL_PRIVATE_UPDATE_FORBIDDEN);
    }
    if (channelUpdateRequest.name() != null && !channelUpdateRequest.name().isBlank()) {
      if (!channel.getName().equals(channelUpdateRequest.name())
          && this.channelRepository.existByName(channelUpdateRequest.name())) {
        throw new ApiException(CHANNEL_NAME_DUPLICATED);
      }
      channel.updateName(channelUpdateRequest.name());
    }

    if (channelUpdateRequest.description() != null) {
      channel.updateDescription(channelUpdateRequest.description());
    }

    Instant lastMessageAt = this.messageRepository.findTopCreatedAtByChannelOrderByCreatedAtDesc(
            channel)
        .orElse(null);
    List<User> participants = this.readStatusRepository.findAllByChannel(channel)
        .stream()
        .map(ReadStatus::getUser)
        .toList();

    log.info("{} channel has been updated successfully. ✅ [ID: {}]", channel.getName(),
        channel.getId());
    return this.toResponse(channel, lastMessageAt, participants);
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

  private ChannelResponse toResponse(Channel channel, Instant lastMessageAt,
      List<User> participants) {
    return new ChannelResponse(
        channel.getId(),
        channel.getName(),
        channel.getDescription(),
        channel.getType(),
        lastMessageAt,
        !channel.isPrivate() ? null :
            participants.stream()
            .map(User::getId)
            .toList()
    );
  }
}
