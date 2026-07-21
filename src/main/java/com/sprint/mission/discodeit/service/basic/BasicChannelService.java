package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateDeniedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;
  private final SseService sseService;

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto createPublic(PublicChannelCreateRequest request) {
    log.debug("PUBLIC 채널 생성 시작: - name: {}", request.name());

    Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
    channelRepository.save(channel);
    ChannelDto dto = channelMapper.toDto(channel);
    sseService.broadcast("channels.created", dto);

    log.info("PUBLIC 채널 생성 완료: - channelId: {}", channel.getId());

    return dto;
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
    log.debug("PRIVATE 채널 생성 시작 - participantCount: {}", request.participantIds());
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel savedChannel = channelRepository.save(channel);

    if (request.participantIds() != null) {
      for (UUID userId : request.participantIds()) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
              log.warn("PRIVATE 채널 생성 실패(참여자에 존재하지 않은 유저 포함) - userId: {}", userId);
              return new UserNotFoundException(userId);
            });
        ReadStatus readStatus = new ReadStatus(user, savedChannel, savedChannel.getCreatedAt());
        savedChannel.getReadStatuses().add(readStatus);
      }
    }

    ChannelDto dto = channelMapper.toDto(channel);
    if (request.participantIds() != null && !request.participantIds().isEmpty()) {
      sseService.send(request.participantIds(), "channels.created", dto);
    }

    log.info("PRIVATE 채널 생성 완료 - channelId: {}", savedChannel.getId());

    return dto;
  }

  @Override
  public ChannelDto findById(UUID id) {
    Channel channel = channelRepository.findById(id).
        orElseThrow(() -> {
          log.warn("채널 조회 실패(존재하지 않은 채널) - channelId: {}", id);
          return new ChannelNotFoundException(id);
        });
    return channelMapper.toDto(channel);
  }

  @Override
  @Cacheable(cacheNames = "channels", key = "#userId")
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> userPrivateChannelIds = readStatusRepository.findByUserId(userId).
        stream().map(readStatus -> readStatus.getChannel().getId()).toList();
    return channelRepository.findAll().stream().filter(
        channel ->
            channel.getType() == ChannelType.PUBLIC ||
                userPrivateChannelIds.contains(channel.getId())
    ).map(channelMapper::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto update(UUID id, ChannelUpdateRequest request) {
    log.debug("채널 수정 시작 - channelId: {}", id);
    Channel channel = channelRepository.findById(id).
        orElseThrow(() -> {
          log.warn("채널 수정 실패(존재하지 않은 채널) - channelId: {}", id);
          return new ChannelNotFoundException(id);
        });

    channel.update(request.newName(), request.newDescription());
    ChannelDto dto = channelMapper.toDto(channel);
    sseService.broadcast("channels.created", dto);

    log.info("채널 수정 완료 - channelId: {}", id);

    return dto;
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public void delete(UUID id) {
    log.debug("채널 삭제 시작 - channelId: {}", id);
    ChannelDto deletedChannelDto = findById(id);

    if (!channelRepository.existsById(id)) {
      log.warn("채널 삭제 실패(존재하지 않는 채널) - channelId: {}", id);
      throw new ChannelNotFoundException(id);
    }

    channelRepository.deleteById(id);
    sseService.broadcast("channels.deleted", deletedChannelDto);

    log.info("채널 삭제 완료 - channelId: {}", id);
  }

}
