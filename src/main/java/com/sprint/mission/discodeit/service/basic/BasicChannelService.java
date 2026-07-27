package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreatedEvent;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto createPublicChannel(PublicChannelRequest request) {
    log.debug("Public 채널 생성 비즈니스 로직 시작 - name: {}, description: {}", request.name(),
        request.description());
    Channel channel = new Channel(
        request.name(),
        request.description(),
        ChannelType.PUBLIC
    );
    Channel savedChannel = channelRepository.save(channel);

    ChannelDto dto = channelMapper.toDto(savedChannel, savedChannel.getCreatedAt(),
        Collections.emptyList());
    applicationEventPublisher.publishEvent(new ChannelCreatedEvent(dto, Instant.now()));

    log.info("Public 채널 생성 완료 - channelId: {}, name: {}", savedChannel.getId(),
        savedChannel.getName());
    return dto;
  }

  @Override
  @Transactional
  public ChannelDto createPrivateChannel(PrivateChannelRequest request) {
    log.debug("Private 채널 생성 비즈니스 로직 시작 - participantCount: {}", request.participantIds().size());
    Channel channel = new Channel(
        "Private Channel",
        "",
        ChannelType.PRIVATE
    );

    Channel createdChannel = channelRepository.save(channel);

    List<UserDto> participants = new ArrayList<>();

    request.participantIds().forEach(userId -> {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> {
            log.warn("Private 채널 생성 실패 - 유저를 찾을 수 없음 - userId: {}", userId);
            return new UserNotFoundException(userId);
          });
      ReadStatus readStatus = new ReadStatus(user, createdChannel, createdChannel.getCreatedAt(),
          true);
      readStatusRepository.save(readStatus);

      participants.add(userMapper.toDto(user, false));
    });

    applicationEventPublisher.publishEvent(
        new PrivateChannelCreatedEvent(request.participantIds())
    );

    ChannelDto dto = channelMapper.toDto(createdChannel, createdChannel.getCreatedAt(),
        participants);
    applicationEventPublisher.publishEvent(new ChannelCreatedEvent(dto, Instant.now()));

    log.info("Private 채널 생성 완료 - channelId: {}, participantCount: {}", createdChannel.getId(),
        participants.size());
    return dto;
  }

  @Override
  public ChannelDto readChannel(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));

    List<UserDto> participants = Collections.emptyList();
    if (channel.getType() == ChannelType.PRIVATE) {
      participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
          .map(rs -> userMapper.toDto(rs.getUser(), false))
          .toList();
    }

    Instant lastMessageAt = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(id)
        .map(Message::getCreatedAt)
        .orElse(channel.getCreatedAt());

    return channelMapper.toDto(channel, lastMessageAt, participants);
  }

  @Override
  @Cacheable(cacheNames = "channels", key = "#userId")
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
                (ReadStatus rs) -> userMapper.toDto(rs.getUser(), false),
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
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public void deleteChannel(UUID id) {
    log.debug("채널 삭제 비즈니스 로직 시작 - channelId: {}", id);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("삭제할 채널이 존재하지 않음 - channelId: {}", id);
          return new ChannelNotFoundException(id);
        });
    ChannelDto dto = channelMapper.toDto(channel, channel.getCreatedAt(), Collections.emptyList());

    channelRepository.delete(channel);

    applicationEventPublisher.publishEvent(new ChannelDeletedEvent(dto, Instant.now()));
    log.info("채널 삭제 성공 - channelId: {}", id);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto updateChannel(UUID id, ChannelUpdateRequest request) {
    log.debug("채널 업데이트 비즈니스 로직 시작 - channelId: {}", id);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("업데이트 할 채널이 존재하지 않음 - channelId: {}", id);
          return new ChannelNotFoundException(id);
        });

    if (channel.getType() == ChannelType.PRIVATE) {
      log.warn("Private 채널은 수정될 수 없음 - channelId: {}", id);
      throw new PrivateChannelUpdateException(id);
    }

    String name = (request.newName() != null) ? request.newName() : channel.getName();
    String description =
        (request.newDescription() != null) ? request.newDescription() : channel.getDescription();

    channel.update(name, description);

    ChannelDto dto = readChannel(channel.getId());
    applicationEventPublisher.publishEvent(new ChannelUpdatedEvent(dto, Instant.now()));

    log.info("채널 업데이트 완료 - channelId: {}, name: {}, description: {}", id, name, description);
    return dto;
  }
}
