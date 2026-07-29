package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.event.dto.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.dto.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.dto.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.dto.PrivateChannelCreatedEvent;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.InsufficientParticipantsException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateDeniedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final ReadStatusRepository readStatusRepository;
  private final ChannelMapper channelMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto createPublicChannel(PublicChannelCreateRequest dto) {
    log.debug("createPublicChannel 시작 - 입력값: {}", dto);
    Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
    channel = channelRepository.save(channel);

    ChannelDto channelDto = channelMapper.toDto(channel);
    eventPublisher.publishEvent(new ChannelCreatedEvent(channelDto, Instant.now()));

    log.info("퍼블릭 채널 생성 완료 - channelId: {}, channelName: {}", channel.getId(), channel.getName());
    return channelDto;
  }

  @Override
  @Transactional
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto) {
    log.debug("createPrivateChannel 시작 - 입력값: {}", dto);
    if (dto.participantIds() == null || dto.participantIds().size() < 2) {
      throw new InsufficientParticipantsException();
    }

    Channel channel = new Channel(null, null, ChannelType.PRIVATE);
    channelRepository.save(channel); // ReadStatus에 저장하기 전에 먼저 실행

    List<User> participants = userRepository.findAllById(dto.participantIds());

    if (participants.size() != dto.participantIds().size()) {
      // TODO: 커스텀 예외 작성 고려
      throw new IllegalArgumentException("요청한 유저 중 존재하지 않는 회원이 포함되어 있습니다.");
    }

    List<ReadStatus> readStatuses = participants.stream()
        .map(user -> new ReadStatus(user, channel, Instant.now(), true))
        .toList();

    readStatusRepository.saveAll(readStatuses);

    eventPublisher.publishEvent(new PrivateChannelCreatedEvent(dto.participantIds()));

    log.info("프라이빗 채널 생성 완료 - channelId: {}", channel.getId());
    return channelMapper.toDto(channel);
  }

  @Cacheable(cacheNames = "channels", key = "#userId")
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    // 한 유저가 속한 PRIVATE 채팅방과, 공개방인 PUBLIC 채팅방 목록을 보여주는 메서드
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    List<UUID> participatedChannels = readStatusRepository.findAllByUserId(user.getId()).stream()
        .map(ReadStatus::getChannel)
        .map(Channel::getId)
        .toList();

    return channelRepository.findByTypeOrIdIn(ChannelType.PUBLIC, participatedChannels).stream()
        .map(channelMapper::toDto)
        .toList();
  }

  @Override
  public ChannelDto findById(UUID channelId) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(
            () -> new ChannelNotFoundException(channelId));

    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto update(UUID id, PublicChannelUpdateRequest dto) {
    log.debug("update 시작 - 입력값: {}", dto);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      throw new PrivateChannelUpdateDeniedException(id);
    }
    channel.update(dto.newName(), dto.newDescription());

    ChannelDto channelDto = channelMapper.toDto(channel);
    eventPublisher.publishEvent(new ChannelUpdatedEvent(channelDto, Instant.now()));

    log.info("채널 수정 완료 - channelId: {}", channel.getId());
    return channelDto;
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public void delete(UUID id) {
    log.debug("delete 시작 - 입력값: {}", id);

    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));

    channelRepository.delete(channel);

    ChannelDto channelDto = channelMapper.toDto(channel);
    eventPublisher.publishEvent(new ChannelDeletedEvent(channelDto, Instant.now()));

    log.info("채널 삭제 완료 - channelId: {}", channel.getId());
  }
}
