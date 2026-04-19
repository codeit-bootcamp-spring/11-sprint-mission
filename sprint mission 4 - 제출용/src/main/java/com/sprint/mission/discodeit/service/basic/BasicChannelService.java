package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.ArrayList;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Override
  @Transactional
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    if (channelRepository.existsByName(request.getName())) {
      throw DiscodeitDuplicateException.channel(request.getName());
    }

    Channel channel = new Channel(
        ChannelType.PUBLIC,
        request.getName(),
        request.getDescription()
    );

    Channel savedChannel = channelRepository.save(channel);
    return channelMapper.toDto(savedChannel);
  }

  @Override
  @Transactional
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
    if (request.getParticipantIds() == null || request.getParticipantIds().size() < 2) {
      throw DiscodeitInvalidInputException.participantIds("participantIds");
    }

    // private은 name, description이 필요없음.
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel savedChannel = channelRepository.save(channel);

    request.getParticipantIds().forEach(participantId -> {
      User participant = userRepository.findById(participantId)
          .orElseThrow(() -> DiscodeitNotFoundException.user(participantId));

      ReadStatus readStatus = new ReadStatus(participant, savedChannel, Instant.now());
      readStatusRepository.save(readStatus);
    });

    return channelMapper.toDto(savedChannel);
  }

  @Override
  @Transactional(readOnly = true)
  public ChannelDto find(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.channel(id));

    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAllByUserId(UUID userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> DiscodeitNotFoundException.user(userId));

    List<Channel> publicChannels = channelRepository.findAllByType(ChannelType.PUBLIC);

    List<Channel> privateChannels = readStatusRepository.findAllByUser_Id(userId).stream()
        .map(ReadStatus::getChannel)
        .toList();

    return Stream.concat(publicChannels.stream(), privateChannels.stream())
        .map(channelMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void update(ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> DiscodeitNotFoundException.channel(request.getChannelId()));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("private채널은 업데이트가 안됩니다.");
    }

    String newName = request.getNewName();
    String newDescription = request.getNewDescription();

    if (newName == null || newName.isBlank()) {
      newName = channel.getName();
    }
    if (newDescription == null) {
      newDescription = channel.getDescription();
    }

    if (channelRepository.existsByNameAndIdNot(newName, request.getChannelId())) {
      throw DiscodeitDuplicateException.channel(newName);
    }

    channel.updateChannel(channel.getType(), newName, newDescription);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.channel(id));

    messageRepository.deleteAllByChannel_Id(id);
    readStatusRepository.deleteByChannel_Id(id);
    channelRepository.delete(channel);
  }
}