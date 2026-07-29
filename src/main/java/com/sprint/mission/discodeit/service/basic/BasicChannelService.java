package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateDeniedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final ChannelMapper channelMapper;
  private final UserRepository userRepository;
  private final CacheManager cacheManager;
  private final ApplicationEventPublisher eventPublisher;

  //create public channel
  @Transactional
  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(value = CacheConfig.CHANNELS, allEntries = true)
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    // 퍼블릭 채널 생성
    log.debug("퍼블릭 채널 생성 시작 - channelName: {}", request.name());
    Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
    channelRepository.save(channel);
    ChannelDto dto = channelMapper.toDto(channel);
    eventPublisher.publishEvent(new ChannelCreatedEvent(dto));
    log.info("퍼블릭 채널 생성 완료 - channelId: {}", channel.getId());
    return dto;
  }

  //create private channel
  @Transactional
  @Override
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
    // 프라이빗 채널 생성
    log.debug("프라이빗 채널 생성 시작 - participantIds: {}", request.participantIds());
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    // ReadStatus가 Channel을 FK로 참조하므로 생성 전 먼저 저장
    channelRepository.save(channel);

    // ReadStatus 생성
    request.participantIds().forEach(userId -> {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> {
            log.warn("존재하지 않는 유저 - userId: {}", userId);
            return new UserNotFoundException(userId);
          });
      ReadStatus readStatus = new ReadStatus(user, channel);
      readStatusRepository.save(readStatus);
    });

    log.info("프라이빗 채널 생성 완료 - channelId: {}", channel.getId());

    //참여자별 채널 목록 캐시만 선택적으로 무효화
    evictChannelsCacheForUsers(request.participantIds());
    ChannelDto dto = channelMapper.toDto(channel);
    eventPublisher.publishEvent(new ChannelCreatedEvent(dto, request.participantIds()));

    return dto;
  }


  //read
  @Override
  @Transactional(readOnly = true)
  public ChannelDto findById(UUID channelId) {
    Channel channel = findChannelOrThrow(channelId);
    return toChannelDto(channel);
  }

  //readAll
  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAll() {
    return channelRepository.findAll().stream()
        .map(this::toChannelDto)
        .toList();
  }

  @Cacheable(cacheNames = CacheConfig.CHANNELS, key = "#userId")
  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> myChannelIds = readStatusRepository.findAllByUserIdWithChannel(userId)
        .stream()
        .map(rs -> rs.getChannel().getId())
        .toList();

    List<Channel> channels = myChannelIds.isEmpty()
        ? channelRepository.findAllByType(ChannelType.PUBLIC)
        : channelRepository.findAllPublicOrIn(myChannelIds);

    return toChannelDtos(channels);
  }

  //update
  @Transactional
  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(value = CacheConfig.CHANNELS, allEntries = true)
  public ChannelDto update(UUID channelId, ChannelUpdateRequest request) {
    log.debug("채널 업데이트 시작 - channelId: {}", channelId);
    Channel channel = findChannelOrThrow(channelId);

    if (channel.getType() == ChannelType.PRIVATE) {
      log.warn("PRIVATE 채널은 수정할 수 없음 - channelId: {}", channelId);
      throw new PrivateChannelUpdateDeniedException(channelId);
    }

    channel.updateChannelName(request.newName());
    channel.updateChannelDescription(request.newDescription());
    log.info("채널 업데이트 완료 - channelId: {}, newName: {}", channel.getId(),
        channel.getName());
    ChannelDto dto = channelMapper.toDto(channel);
    eventPublisher.publishEvent(new ChannelUpdatedEvent(dto));

    return dto;
  }

  //delete
  @Transactional
  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(value = CacheConfig.CHANNELS, allEntries = true)
  public void delete(UUID channelId) {
    log.debug("채널 삭제 시작 - channelId: {}", channelId);
    Channel channel = findChannelOrThrow(channelId);
    List<UUID> participantIds = channel.getType() == ChannelType.PRIVATE
        ? findParticipants(channel).stream().map(User::getId).toList()
        : List.of();
    channelRepository.deleteById(channelId); // Message, ReadStatus cascade로 자동 삭제
    eventPublisher.publishEvent(new ChannelDeletedEvent(channelId, participantIds));
    log.info("채널 삭제 완료 - channelId: {}", channelId);
  }

  //channel 검증 로직
  private Channel findChannelOrThrow(UUID channelId) {
    return channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("존재하지 않는 채널 - channelId: {}", channelId);
          return new ChannelNotFoundException(channelId);
        });
  }

  //participants 조회 로직
  private List<User> findParticipants(Channel channel) {
    return channel.getType() == ChannelType.PRIVATE
        ? readStatusRepository.findAllByChannel_Id(channel.getId())
        .stream()
        .map(ReadStatus::getUser)
        .toList()
        : null;
  }

  //lastMessageAt 조회 로직
  private Instant findLastMessageAt(Channel channel) {
    return messageRepository
        .findTopByChannel_IdOrderByCreatedAtDesc(channel.getId())
        .map(Message::getCreatedAt)
        .orElse(null);
  }

  // channel, participants, lastMessageAt 단건 변환
  private ChannelDto toChannelDto(Channel channel) {
    return channelMapper.toDto(channel, findParticipants(channel), findLastMessageAt(channel));
  }

  // 다건 변환
  private List<ChannelDto> toChannelDtos(List<Channel> channels) {
    List<UUID> channelIds = channels.stream()
        .map(Channel::getId)
        .toList();

    // 참여자 벌크 조회 → channelId로 그룹핑
    Map<UUID, List<User>> participantsMap = readStatusRepository
        .findAllByChannelIds(channelIds)
        .stream()
        .collect(Collectors.groupingBy(
            rs -> rs.getChannel().getId(),
            Collectors.mapping(ReadStatus::getUser, Collectors.toList())
        ));

    // 마지막 메시지 벌크 조회 → channelId로 그룹핑
    Map<UUID, Instant> lastMessageAtMap = messageRepository
        .findLastMessagesByChannelIds(channelIds)
        .stream()
        .collect(Collectors.toMap(
            m -> m.getChannel().getId(),
            Message::getCreatedAt
        ));

    return channels.stream()
        .map(channel -> {
          List<User> participants = channel.getType() == ChannelType.PRIVATE
              ? participantsMap.getOrDefault(channel.getId(), List.of())
              : null;
          Instant lastMessageAt = lastMessageAtMap.get(channel.getId());
          return channelMapper.toDto(channel, participants, lastMessageAt);
        })
        .toList();
  }

  private void evictChannelsCacheForUsers(List<UUID> userIds) {
    Cache cache = cacheManager.getCache(CacheConfig.CHANNELS);
    if (cache != null) {
      userIds.forEach(cache::evict);
    }
  }
}
