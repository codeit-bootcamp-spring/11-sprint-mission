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
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
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

  private final UserMapper userMapper;
  private final ChannelMapper channelMapper;

  @Override
  @Transactional
  public ChannelDto createPublicChannel(PublicChannelRequest request) {
    Channel channel = new Channel(
        request.name(),
        request.description(),
        ChannelType.PUBLIC
    );
    Channel savedChannel = channelRepository.save(channel);

    return channelMapper.toDto(savedChannel, savedChannel.getCreatedAt(), Collections.emptyList());
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

      participants.add(userMapper.toDto(user, user.getStatus()));
    });

    return channelMapper.toDto(createdChannel, createdChannel.getCreatedAt(), participants);
  }

  @Override
  public ChannelDto readChannel(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));

    List<UserDto> participants = Collections.emptyList();
    if (channel.getType() == ChannelType.PRIVATE) {
      participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
          .map(rs -> userMapper.toDto(rs.getUser(), rs.getUser().getStatus()))
          .toList();
    }

    Instant lastMessageAt = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(id)
        .map(Message::getCreatedAt)
        .orElse(channel.getCreatedAt());

    return channelMapper.toDto(channel, lastMessageAt, participants);
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<Channel> allChannels = channelRepository.findAll();

    Set<UUID> joinChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(rs -> rs.getChannel().getId())
        .collect(Collectors.toSet());

    List<Channel> targetChannels = allChannels.stream()
        .filter(c -> c.getType() == ChannelType.PUBLIC || joinChannelIds.contains(c.getId()))
        .toList();

    List<UUID> targetChannelIds = targetChannels.stream()
        .map(Channel::getId)
        .toList();

    List<Message> latestMessages = messageRepository.findLatestMessagesByChannelIds(
        targetChannelIds);
    Map<UUID, Instant> lastestMessageMap = latestMessages.stream()
        .collect(Collectors.toMap(m -> m.getChannel().getId(), Message::getCreatedAt,
            (existing, replacement) -> existing));

    List<UUID> privateChannelIds = targetChannels.stream()
        .filter(c -> c.getType() == ChannelType.PRIVATE)
        .map(Channel::getId)
        .toList();
    Map<UUID, List<UserDto>> participantMap = readStatusRepository.findAllByChannelIdIn(
            privateChannelIds).stream()
        .collect(Collectors.groupingBy((ReadStatus rs) -> rs.getChannel().getId(),
            Collectors.mapping(
                (ReadStatus rs) -> userMapper.toDto(rs.getUser(), rs.getUser().getStatus()),
                Collectors.toList()
            )
        ));

    return targetChannels.stream()
        .map(channel -> {
          Instant lastMessageAt = lastestMessageMap.getOrDefault(channel.getId(),
              channel.getCreatedAt());
          List<UserDto> participants = participantMap.getOrDefault(channel.getId(),
              Collections.emptyList());

          return channelMapper.toDto(channel, lastMessageAt, participants);
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

    String name = (request.newName() != null) ? request.newName() : channel.getName();
    String description =
        (request.newDescription() != null) ? request.newDescription() : channel.getDescription();

    channel.update(name, description);

    return readChannel(channel.getId());
  }
}
