package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

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
    Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
    channel = channelRepository.save(channel);
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto) {
    if (dto.participantIds() == null || dto.participantIds().size() < 2) {
      throw new IllegalArgumentException(
          "At least 2 users are required to create a private channel");
    }
    Channel channel = new Channel(null, null, ChannelType.PRIVATE);
    channelRepository.save(channel); // ReadStatus에 저장하기 전에 먼저 실행
    dto.participantIds().forEach(id -> {
      User user = userRepository.findById(id)
          .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));
      ReadStatus status = new ReadStatus(user, channel, Instant.now());
      readStatusRepository.save(status);
    });
    return channelMapper.toDto(channel);
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    // 한 유저가 속한 PRIVATE 채팅방과, 공개방인 PUBLIC 채팅방 목록을 보여주는 메서드
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));

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
            () -> new NoSuchElementException("Channel with id " + channelId + " not found"));

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
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Channel with id " + id + " not found"));
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      throw new IllegalArgumentException("Private channel cannot be updated");
    }
    channel.update(dto.newName(), dto.newDescription());
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Channel with id " + id + " not found"));

    channelRepository.delete(channel);
  }

}
