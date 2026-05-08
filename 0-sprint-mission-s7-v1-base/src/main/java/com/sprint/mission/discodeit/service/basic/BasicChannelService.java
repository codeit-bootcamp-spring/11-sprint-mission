package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Transactional
  @Override
  public ChannelDto create(PublicChannelCreateRequest request) {
    String name = request.name();
    String description = request.description();

    log.info("공개 채널 생성 처리 시작.");
    log.debug("name={}, description={}", name, description);

    Channel channel = new Channel(ChannelType.PUBLIC, name, description);
    channelRepository.save(channel);

    log.info("공개 채널 생성 처리 완료. channelId={}", channel.getId());

    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public ChannelDto create(PrivateChannelCreateRequest request) {
    log.info("비공개 채널 생성 처리 시작.");
    log.debug("participantIds={}", request.participantIds());

    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    channelRepository.save(channel);

    List<ReadStatus> readStatuses = userRepository.findAllById(request.participantIds()).stream()
        .map(user -> new ReadStatus(user, channel, channel.getCreatedAt()))
        .toList();
    readStatusRepository.saveAll(readStatuses);

    log.info("비공개 채널 생성 처리 완료. channelId={}", channel.getId());

    return channelMapper.toDto(channel);
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto find(UUID channelId) {
    log.debug("채널 단건 조회 처리 시작. channelId={}", channelId);

    ChannelDto channelDto = channelRepository.findById(channelId)
        .map(channelMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " not found"));

    log.debug("채널 단건 조회 처리 완료. channelId={}", channelId);

    return channelDto;
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    log.debug("사용자 채널 목록 조회 처리 시작. userId={}", userId);

    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannel)
        .map(Channel::getId)
        .toList();

    List<ChannelDto> channelDtos = channelRepository.findAllByTypeOrIdIn(
            ChannelType.PUBLIC,
            mySubscribedChannelIds
        )
        .stream()
        .map(channelMapper::toDto)
        .toList();

    log.info("사용자 채널 목록 조회 처리 완료. userId={}, count={}", userId, channelDtos.size());

    return channelDtos;
  }

  @Transactional
  @Override
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    String newName = request.newName();
    String newDescription = request.newDescription();

    log.info("채널 수정 처리 시작. channelId={}", channelId);
    log.debug("newName={}, newDescription={}", newName, newDescription);

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " not found"));

    if (channel.getType().equals(ChannelType.PRIVATE)) {
      log.warn("비공개 채널 수정 시도. channelId={}", channelId);
      throw new IllegalArgumentException("Private channel cannot be updated");
    }

    channel.update(newName, newDescription);

    log.info("채널 수정 처리 완료. channelId={}", channelId);

    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public void delete(UUID channelId) {
    log.info("채널 삭제 처리 시작. channelId={}", channelId);

    if (!channelRepository.existsById(channelId)) {
      log.warn("존재하지 않는 채널 삭제 시도. channelId={}", channelId);
      throw new NoSuchElementException("Channel with id " + channelId + " not found");
    }

    messageRepository.deleteAllByChannelId(channelId);
    readStatusRepository.deleteAllByChannelId(channelId);
    channelRepository.deleteById(channelId);

    log.info("채널 삭제 처리 완료. channelId={}", channelId);
  }
}
