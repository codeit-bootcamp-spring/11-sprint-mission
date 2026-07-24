package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto.Response createPublicChannel(ChannelDto.CreatePublicRequest request) {
    log.debug("퍼블릭 채널 생성 시작: name={}", request.name());

    Channel channel = request.toEntity();
    channelRepository.save(channel);

    log.info("퍼블릭 채널 생성 완료: channelId={}, name={}", channel.getId(), channel.getName());
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto.Response createPrivateChannel(ChannelDto.CreatePrivateRequest request) {
    List<UUID> participantIds =
        request.participantIds() != null ? request.participantIds() : Collections.emptyList();
    log.debug("프라이빗 채널 생성 시작: 참여자 수={}", participantIds.size());

    List<User> participants = userRepository.findAllById(participantIds);

    if (participants.size() != participantIds.size()) {
      log.warn("유효하지 않은 유저 ID가 포함되어 있습니다. (요청: {}명, 실제: {}명)", participantIds.size(),
          participants.size());
      throw UserNotFoundException.withIds(participantIds);
    }

    Channel channel = request.toEntity();
    channelRepository.save(channel);

    if (!participants.isEmpty()) {
      List<ReadStatus> readStatuses = participants.stream()
          .map(user -> ReadStatus.builder()
              .user(user)
              .channel(channel)
              .lastReadAt(channel.getCreatedAt())
              .notificationEnabled(true)
              .build())
          .toList();

      readStatusRepository.saveAll(readStatuses);
    }
    log.info("프라이빗 채널 생성 완료: channelId={}", channel.getId());
    return channelMapper.toDto(channel);
  }

  @Override
  public ChannelDto.Response findById(UUID id) {
    log.debug("채널 단건 조회 시작: id={}", id);

    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> ChannelNotFoundException.withId(id));

    log.info("채널 단건 조회 완료: id={}", id);
    return channelMapper.toDto(channel);
  }

  @Override
  @Cacheable(cacheNames = "channels", key = "#userId")
  public List<ChannelDto.Response> findAllByUserId(UUID userId) {
    log.debug("사용자 소속 채널 목록 조회 시작: userId={}", userId);

    List<UUID> myChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(readStatus -> readStatus.getChannel().getId())
        .toList();

    List<ChannelDto.Response> responses = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC,
            myChannelIds)
        .stream()
        .map(channelMapper::toDto)
        .toList();

    log.info("사용자 소속 채널 목록 조회 완료: 총 {}건", responses.size());
    return responses;
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public ChannelDto.Response update(UUID id, ChannelDto.UpdateRequest request) {
    log.debug("채널 업데이트 시작: channelId={}", id);

    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> ChannelNotFoundException.withId(id));

    channel.updatePublicInfo(request.newName(), request.newDescription());

    log.info("채널 업데이트 완료: channelId={}", id);
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  @CacheEvict(cacheNames = "channels", allEntries = true)
  public void delete(UUID id) {
    log.debug("채널 삭제 시작: channelId={}", id);

    if (!channelRepository.existsById(id)) {
      throw ChannelNotFoundException.withId(id);
    }
    messageRepository.deleteByChannelId(id);
    readStatusRepository.deleteByChannelId(id);

    channelRepository.deleteById(id);
    log.info("채널 삭제 완료: channelId={}", id);
  }
}