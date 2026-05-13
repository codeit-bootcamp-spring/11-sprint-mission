package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.*;
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

  @Override
  @Transactional
  public ChannelDto createPublicChannel(PublicChannelCreateRequest dto) {
    log.debug("createPublicChannel 시작 - 입력값: {}", dto);
    Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
    channel = channelRepository.save(channel);

    log.info("퍼블릭 채널 생성 완료 - channelId: {}, channelName: {}", channel.getId(), channel.getName());
    return channelMapper.toDto(channel);
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

    dto.participantIds().forEach(id -> {
      User user = userRepository.findById(id)
          .orElseThrow(() -> new UserNotFoundException(id));
      ReadStatus status = new ReadStatus(user, channel, Instant.now());
      readStatusRepository.save(status);
    });

    log.info("프라이빗 채널 생성 완료 - channelId: {}", channel.getId());
    return channelMapper.toDto(channel);
  }

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

//  @Override // 사용되는 곳이 없음
//  public List<UUID> getUserIds(UUID id) {
//    Channel channel = channelRepository.findById(id)
//        .orElseThrow(() -> new NoSuchElementException("Channel with id " + id + " not found"));
//    List<ReadStatus> readStatusList = readStatusRepository.findAllByChannelId(channel.getId());
//    return readStatusList.stream()
//        .map(ReadStatus::getUser)
//        .map(User::getId)
//        .toList();
//  }

  @Override
  @Transactional
  public ChannelDto update(UUID id, PublicChannelUpdateRequest dto) {
    log.debug("update 시작 - 입력값: {}", dto);
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      throw new PrivateChannelUpdateDeniedException(id);
    }
    channel.update(dto.newName(), dto.newDescription());

    log.info("채널 수정 완료 - channelId: {}", channel.getId());
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("delete 시작 - 입력값: {}", id);

    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new ChannelNotFoundException(id));

    channelRepository.delete(channel);
    log.info("채널 삭제 완료 - channelId: {}", channel.getId());
  }
}
