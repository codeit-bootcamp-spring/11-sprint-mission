package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {
    if (channelRepository.existsByName(request.getName())) {
      throw DiscodeitDuplicateException.channel(request.getName());
    }

    Channel channel = new Channel(ChannelType.PUBLIC, request.getName(), request.getDescription());
    Channel savedChannel = channelRepository.save(channel);

    return new ChannelResponse(
        savedChannel.getId(),
        savedChannel.getCreatedAt(),
        savedChannel.getUpdatedAt(),
        savedChannel.getType(),
        savedChannel.getName(),
        savedChannel.getDescription(),
        null,
        null
    );
  }

  @Override
  @Transactional
  public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
    if (request.getParticipantIds() == null || request.getParticipantIds().size() < 2) {
      throw DiscodeitInvalidInputException.participantIds("participantIds");
    }

    // private은 name, description이 필요없음.
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel savedChannel = channelRepository.save(channel);

    request.getParticipantIds().forEach(participantId -> {
      User participant = userRepository.findById(participantId)
          .orElseThrow(() -> DiscodeitNotFoundException.user(participantId));

      ReadStatus readStatus = new ReadStatus(participant, savedChannel, Instant.now());
      readStatusRepository.save(readStatus);
    });

    return new ChannelResponse(
        savedChannel.getId(),
        savedChannel.getCreatedAt(),
        savedChannel.getUpdatedAt(),
        savedChannel.getType(),
        savedChannel.getName(),
        savedChannel.getDescription(),
        request.getParticipantIds(),
        null
    );
  }

  @Override
  @Transactional(readOnly = true)
  public ChannelResponse find(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.channel(id));

    Instant lastMessageAt = messageRepository.findAllByChannel_Id(id).stream()
        .map(message -> message.getCreatedAt())
        .max(Comparator.naturalOrder())
        .orElse(null);

    List<UUID> participantIds = null;
    if (channel.getType() == ChannelType.PRIVATE) {
      participantIds = readStatusRepository.findAllByChannel_Id(id).stream()
          .map(readStatus -> readStatus.getUser().getId())
          .toList();
    }
    return new ChannelResponse(
        channel.getId(),
        channel.getCreatedAt(),
        channel.getUpdatedAt(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        participantIds,
        lastMessageAt
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    // public 채널 가져오기
    List<Channel> publicChannels = channelRepository.findAllByType(ChannelType.PUBLIC);

    // 사용자의 readStatus -> private 채널 가져오기
    List<Channel> privateChannels = readStatusRepository.findAllByUser_Id(userId).stream()
        .map(ReadStatus::getChannel)
        .toList();

    // public, private 하나로 이어붙이기
    List<Channel> channels = Stream.concat(publicChannels.stream(), privateChannels.stream())
        .distinct()
        .toList();

    return channels.stream()
        .map(channel -> {
          Instant lastMessageAt = messageRepository.findAllByChannel_Id(channel.getId()).stream()
              .map(Message::getCreatedAt)
              .max(Comparator.naturalOrder())
              .orElse(null);

          List<UUID> participantIds = null;
          if (channel.getType() == ChannelType.PRIVATE) {
            participantIds = readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
                .map(readStatus -> readStatus.getUser().getId())
                .toList();
          }

          return new ChannelResponse(
              channel.getId(),
              channel.getCreatedAt(),
              channel.getUpdatedAt(),
              channel.getType(),
              channel.getName(),
              channel.getDescription(),
              participantIds,
              lastMessageAt
          );
        })
        .toList();
  }

  @Override
  @Transactional
  public void update(ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> DiscodeitNotFoundException.channel(request.getChannelId()));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("private채널은 업데이트가 안됩니다.");
    }

    String newName = request.getNewName();
    String newDescription = request.getNewDescription();

    if (newName == null || newName.isBlank()) {
      newName = channel.getName();
    }
    if (newDescription == null) {
      newDescription = channel.getDescription();
    }

    if (channelRepository.existsByNameAndIdNot(newName, request.getChannelId())) {
      throw DiscodeitDuplicateException.channel(newName);
    }

    channel.updateChannel(channel.getType(), newName, newDescription);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.channel(id));

    messageRepository.deleteAllByChannel_Id(id);
    readStatusRepository.deleteByChannel_Id(id);
    channelRepository.delete(channel);
  }
}