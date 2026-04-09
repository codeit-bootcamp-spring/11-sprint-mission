package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
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
  public ChannelDto createPublicChannel(PublicChannelRequest request) {
    Channel channel = new Channel(
        request.name(),
        request.description(),
        ChannelType.PUBLIC
    );
    Channel savedChannel = channelRepository.save(channel);

    return ChannelDto.from(savedChannel, savedChannel.getCreatedAt(), Collections.emptyList());
  }

  @Override
  @Transactional
  public ChannelDto createPrivateChannel(PrivateChannelRequest request) {
    Channel channel = new Channel(
        "Private Channel",
        "",
        ChannelType.PRIVATE
    );

    Channel createdChannel = channelRepository.save(channel);

    List<UserDto> participants = new ArrayList<>();

    request.participantIds().forEach(userId -> {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
      ReadStatus readStatus = new ReadStatus(user, createdChannel, createdChannel.getCreatedAt());
      readStatusRepository.save(readStatus);

      participants.add(UserDto.from(user, user.getStatus()));
    });

    return ChannelDto.from(createdChannel, createdChannel.getCreatedAt(), participants);
  }

  @Override
  public ChannelDto readChannel(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    List<UserDto> participants = Collections.emptyList();
    if (channel.getType() == ChannelType.PRIVATE) {
      participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
          .map(rs -> UserDto.from(rs.getUser(), rs.getUser().getStatus()))
          .toList();
    }

    Instant lastMessageAt = messageRepository.findLatestMessage(id)
        .map(Message::getCreatedAt)
        .orElse(channel.getCreatedAt());

    return ChannelDto.from(channel, lastMessageAt, participants);
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<Channel> allChannels = channelRepository.findAll();

    Set<UUID> joinChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(rs -> rs.getChannel().getId())
        .collect(Collectors.toSet());

    return allChannels.stream()
        .filter(c -> c.getType() == ChannelType.PUBLIC || joinChannelIds.contains(c.getId()))
        .map(channel -> {
          // N + 1 발생가능 지점
          Instant lastMessageAt = messageRepository.findLatestMessage(channel.getId())
              .map(Message::getCreatedAt)
              .orElse(channel.getCreatedAt());

          List<UserDto> participants = Collections.emptyList();
          if (channel.getType() == ChannelType.PRIVATE) {
            participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(rs -> UserDto.from(rs.getUser(), rs.getUser().getStatus()))
                .toList();
          }
          return ChannelDto.from(channel, lastMessageAt, participants);
        })
        .sorted(Comparator.comparing(ChannelDto::lastMessageAt).reversed())
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
  public ChannelDto updateChannel(UUID id, ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new BusinessLogicException(ExceptionCode.CHANNEL_MODIFY_PRIVATE);
    }

    String name = (request.name() != null) ? request.name() : channel.getName();
    String description =
        (request.description() != null) ? request.description() : channel.getDescription();

    channel.update(name, description);

    return readChannel(channel.getId());
  }
}
