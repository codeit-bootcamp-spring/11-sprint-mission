package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.ChannelType.PUBLIC;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;

  @Override
  public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {
    if (channelRepository.existsByChannelName(request.getName())) {
      throw DiscodeitDuplicateException.channel(request.getName());
    }

    Channel channel = new Channel(request.getName(), request.getDescription(), ChannelType.PUBLIC);
    channelRepository.create(channel);

    return new ChannelResponse(
        channel.getId(),
        channel.getCreatedAt(),
        channel.getUpdatedAt(),
        channel.getChannelType(),
        channel.getChannelName(),
        channel.getChannelDescription(),
        null,
        null
    );
  }

  @Override
  public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
    Channel channel = new Channel(null, null, ChannelType.PRIVATE);
    channelRepository.create(channel);

    request.getParticipantIds().forEach(participantId -> {
      ReadStatus readStatus = new ReadStatus(participantId, channel.getId(), Instant.now());
      readStatusRepository.create(readStatus);
    });

    return new ChannelResponse(
        channel.getId(),
        channel.getCreatedAt(),
        channel.getUpdatedAt(),
        channel.getChannelType(),
        channel.getChannelName(),
        channel.getChannelDescription(),
        request.getParticipantIds(),
        null
    );
  }

  @Override
  public ChannelResponse read(UUID id) {
    Channel channel = channelRepository.read(id);
    if (channel == null) {
      throw DiscodeitNotFoundException.channel(id);
    }

    Instant lastMessageAt = messageRepository.readAllByChannelId(id).stream()
        .map(message -> message.getCreatedAt())
        .max(Comparator.naturalOrder())
        .orElse(null);

    List<UUID> participantIds = null;
    if (channel.getChannelType() == ChannelType.PRIVATE) {
      participantIds = readStatusRepository.readAllByChannelId(id).stream()
          .map(ReadStatus::getUserId)
          .toList();
    }
    return new ChannelResponse(
        channel.getId(),
        channel.getCreatedAt(),
        channel.getUpdatedAt(),
        channel.getChannelType(),
        channel.getChannelName(),
        channel.getChannelDescription(),
        participantIds,
        lastMessageAt
    );
  }

  @Override
  public List<ChannelResponse> readAllByUserId(UUID userId) {
    return channelRepository.readAll().stream()
        .filter(channel -> channel.getChannelType() == ChannelType.PUBLIC
            || readStatusRepository.readAllByChannelId(channel.getId()).stream()
            .anyMatch(readStatus -> readStatus.getUserId().equals(userId)))
        .map(channel -> {
          Instant lastMessageAt = messageRepository.readAllByChannelId(channel.getId()).stream()
              .map(message -> message.getCreatedAt())
              .max(Comparator.naturalOrder())
              .orElse(null);

          List<UUID> participantIds = null;
          if (channel.getChannelType() == ChannelType.PRIVATE) {
            participantIds = readStatusRepository.readAllByChannelId(channel.getId()).stream()
                .map(ReadStatus::getUserId)
                .toList();
          }

          return new ChannelResponse(
              channel.getId(),
              channel.getCreatedAt(),
              channel.getUpdatedAt(),
              channel.getChannelType(),
              channel.getChannelName(),
              channel.getChannelDescription(),
              participantIds,
              lastMessageAt
          );
        })
        .toList();
  }

  @Override
  public void update(ChannelUpdateRequest request) {
    Channel channel = channelRepository.read(request.getChannelId());
    if (channel == null) {
      throw DiscodeitNotFoundException.channel(request.getChannelId());
    }

    if (channel.getChannelType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("private채널은 업데이트가 안됩니다.");
    }

    String newName = request.getNewName();
    String newDescription = request.getNewDescription();

    if (newName == null || newName.isBlank()) {
      newName = channel.getChannelName();
    }
    if (newDescription == null) {
      newDescription = channel.getChannelDescription();
    }

    if (channelRepository.existsByChannelNameExcluding(newName, request.getChannelId())) {
      throw DiscodeitDuplicateException.channel(newName);
    }

    channel.updateChannel(newName, newDescription);
    channelRepository.update(channel);
  }

  @Override
  public void delete(UUID id) {
    Channel channel = channelRepository.read(id);
    if (channel == null) {
      throw DiscodeitNotFoundException.channel(id);
    }

    messageRepository.deleteAllByChannelId(id);
    readStatusRepository.deleteByChannelId(id);
    channelRepository.delete(id);
  }

  @Override
  public void restore(UUID id) {
    channelRepository.restore(id);
  }
}