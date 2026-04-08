package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;

  @Override
  public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {
    Channel channel = new Channel(request.channelName(), request.description());
    Channel savedChannel = channelRepository.save(channel);

    Instant lastMessageAt = null;
    return ChannelResponse.of(savedChannel, lastMessageAt);
  }

  @Override
  public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
    List<UUID> participantIds = request.participantIds();

    for (UUID participantId : participantIds) {
      if (userRepository.findById(participantId) == null) {
        throw new IllegalArgumentException("존재하지 않는 유저입니다.");
      }
    }

    Channel channel = new Channel(participantIds);
    Channel savedChannel = channelRepository.save(channel);

    for (UUID participantId : participantIds) {
      ReadStatus readStatus = new ReadStatus(participantId, savedChannel.getId());
      readStatusRepository.save(readStatus);
    }

    Instant lastMessageAt = null;
    return ChannelResponse.of(savedChannel, lastMessageAt);
  }

  @Override
  public ChannelResponse findById(UUID id) {
    Channel channel = channelRepository.findById(id);

    if (channel == null) {
      throw new IllegalStateException("존재하지 않는 채널입니다.");
    }

    Instant lastMessageAt = messageRepository.findAll().stream()
        .filter(message -> message.getChannelId().equals(id))
        .map(message -> message.getCreatedAt())
        .max(Instant::compareTo)
        .orElse(null);

    return ChannelResponse.of(channel, lastMessageAt);
  }

  @Override
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    if (userRepository.findById(userId) == null) {
      throw new IllegalStateException("존재하지 않는 유저입니다.");
    }

    List<Message> allMessages = messageRepository.findAll();

    return channelRepository.findAll().stream()
        .filter(channel ->
            channel.getType() == ChannelType.PUBLIC ||
                channel.getParticipantIds().contains(userId)
        )
        .map(channel -> {
          Instant lastMessageAt = allMessages.stream()
              .filter(message -> message.getChannelId().equals(channel.getId()))
              .map(Message::getCreatedAt)
              .max(Instant::compareTo)
              .orElse(null);

          return ChannelResponse.of(channel, lastMessageAt);
        })
        .toList();
  }

  @Override
  public ChannelResponse update(UUID id, ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(id);

    if (channel == null) {
      throw new IllegalStateException("존재하지 않는 채널입니다.");
    }

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalStateException("PRIVATE 채널은 수정할 수 없습니다.");
    }

    channel.update(request.channelName(), request.description());
    Channel updatedChannel = channelRepository.save(channel);

    Instant lastMessageAt = messageRepository.findAll().stream()
        .filter(message -> message.getChannelId().equals(updatedChannel.getId()))
        .map(message -> message.getCreatedAt())
        .max(Instant::compareTo)
        .orElse(null);

    return ChannelResponse.of(updatedChannel, lastMessageAt);
  }

  @Override
  public void delete(UUID id) {
    if (channelRepository.findById(id) == null) {
      throw new IllegalStateException("존재하지 않는 채널입니다.");
    }

    messageRepository.findAll().stream()
        .filter(message -> message.getChannelId().equals(id))
        .forEach(message -> messageRepository.delete(message.getId()));

    readStatusRepository.findAll().stream()
        .filter(readStatus -> readStatus.getChannelId().equals(id))
        .forEach(readStatus -> readStatusRepository.delete(readStatus.getId()));

    channelRepository.delete(id);
  }
}