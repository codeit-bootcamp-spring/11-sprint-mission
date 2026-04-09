package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdate;
import com.sprint.mission.discodeit.dto.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.PublicChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public Channel createPublicChannel(PublicChannelRequest request) {
    Channel channel = new Channel(
        request.name(),
        request.description(),
        ChannelType.PUBLIC
    );
    return channelRepository.save(channel);
  }

  @Override
  @Transactional
  public Channel createPrivateChannel(PrivateChannelRequest request) {
    Channel channel = new Channel(
        "Private Channel",
        "",
        ChannelType.PRIVATE
    );

    Channel createdChannel = channelRepository.save(channel);

    request.participantIds().forEach(userId -> {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
      ReadStatus readStatus = new ReadStatus(user, createdChannel, Instant.MIN);
      readStatusRepository.save(readStatus);
    });

    return createdChannel;
  }

  @Override
  public ChannelResponse readChannel(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    List<UUID> memberIds = null;
    if (channel.getType() == ChannelType.PRIVATE) {
      memberIds = readStatusRepository.findAllByChannelId(channel.getId()).stream()
          .map(rs -> rs.getUser().getId())
          .toList();
    }

    Instant lastMessageAt = messageRepository.findLatestMessage(id)
        .map(Message::getCreatedAt)
        .orElse(channel.getCreatedAt());

    return new ChannelResponse(
        channel.getId(),
        channel.getName(),
        channel.getType(),
        channel.getDescription(),
        lastMessageAt,
        memberIds
    );
  }

  @Override
  public List<ChannelResponse> findAllByUserId(UUID userId) {
    List<Channel> allChannels = channelRepository.findAll();

    Set<UUID> joinChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(rs -> rs.getChannel().getId())
        .collect(Collectors.toSet());

    return allChannels.stream()
        .filter(c -> c.getType() == ChannelType.PUBLIC || joinChannelIds.contains(c.getId()))
        .map(channel -> {
          Instant lastMessageAt = messageRepository.findLatestMessage(channel.getId())
              .map(Message::getCreatedAt)
              .orElse(channel.getCreatedAt());

          List<UUID> memberIds = Collections.emptyList();
          if (channel.getType() == ChannelType.PRIVATE) {
            memberIds = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(rs -> rs.getUser().getId())
                .toList();
          }
          return new ChannelResponse(
              channel.getId(),
              channel.getName(),
              channel.getType(),
              channel.getDescription(),
              lastMessageAt,
              memberIds
          );
        })
        .sorted(Comparator.comparing(ChannelResponse::lastMessageAt).reversed())
        .toList();
  }

  @Override
  @Transactional
  public void deleteChannel(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    channelRepository.delete(channel);
  }

  @Override
  @Transactional
  public void updateChannel(UUID id, ChannelUpdate request) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new BusinessLogicException(ExceptionCode.CHANNEL_MODIFY_PRIVATE);
    }

    String name = (request.name() != null) ? request.name() : channel.getName();
    String description =
        (request.description() != null) ? request.description() : channel.getDescription();

    channel.update(name, description);
  }
}
