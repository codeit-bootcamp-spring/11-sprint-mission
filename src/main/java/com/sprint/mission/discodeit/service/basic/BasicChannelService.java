package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final UserStatusRepository userStatusRepository;
  private final ChannelMapper channelMapper;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public ChannelDto createPublic(PublicChannelCreateRequest request) {
    log.debug("PUBLIC 채널 생성 요청 - name: {}", request.name());
    Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
    channelRepository.save(channel);
    log.info("PUBLIC 채널 생성 완료 - id: {}, name: {}", channel.getId(), channel.getName());
    return toDto(channel);
  }

  @Override
  @Transactional
  public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
    log.debug("PRIVATE 채널 생성 요청 - participantIds: {}", request.participantIds());
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    channelRepository.save(channel);

    for (UUID userId : request.participantIds()) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> {
            log.warn("PRIVATE 채널 생성 실패 - 존재하지 않는 userId: {}", userId);
            return new UserNotFoundException(userId);
          });
      ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
      readStatusRepository.save(readStatus);
    }
    log.info("PRIVATE 채널 생성 완료 - id: {}", channel.getId());
    return toDto(channel);
  }

  @Override
  public ChannelDto findById(UUID id) {
    log.debug("채널 단건 조회 - id: {}", id);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("채널 조회 실패 - 존재하지 않는 id: {}", id);
          return new ChannelNotFoundException(id);
        });
    return toDto(channel);
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    log.debug("사용자별 채널 목록 조회 - userId: {}", userId);
    List<Channel> publicChannels = channelRepository.findAllByType(ChannelType.PUBLIC);
    List<Channel> privateChannels = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannel)
        .filter(ch -> ch.getType() == ChannelType.PRIVATE)
        .toList();

    return java.util.stream.Stream.concat(publicChannels.stream(), privateChannels.stream())
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    log.debug("채널 수정 요청 - id: {}", channelId);
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("채널 수정 실패 - 존재하지 않는 id: {}", channelId);
          return new ChannelNotFoundException(channelId);
        });
    if (channel.getType() == ChannelType.PRIVATE) {
      log.warn("채널 수정 실패 - PRIVATE 채널은 수정 불가 id: {}", channelId);
      throw new ChannelUpdateNotAllowedException(channelId);
    }
    channel.update(request.newName(), request.newDescription());
    log.info("채널 수정 완료 - id: {}", channelId);
    return toDto(channel);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("채널 삭제 요청 - id: {}", id);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("채널 삭제 실패 - 존재하지 않는 id: {}", id);
          return new ChannelNotFoundException(id);
        });
    channelRepository.delete(channel);
    log.info("채널 삭제 완료 - id: {}", id);
  }

  private ChannelDto toDto(Channel channel) {
    Instant lastMessageAt = messageRepository
        .findTopByChannelIdOrderByCreatedAtDesc(channel.getId())
        .map(Message::getCreatedAt)
        .orElse(null);

    List<UserDto> participants = new ArrayList<>();
    if (channel.getType() == ChannelType.PRIVATE) {
      participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
          .map(rs -> {
            UserStatus status = userStatusRepository
                .findByUserId(rs.getUser().getId()).orElse(null);
            return userMapper.toDto(rs.getUser(), status);
          })
          .toList();
    }

    return channelMapper.toDto(channel, participants, lastMessageAt);
  }
}