package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
  public ChannelDto.Response createPublicChannel(ChannelDto.CreatePublicRequest request) {
    Channel channel = request.toEntity();
    channelRepository.save(channel);
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public ChannelDto.Response createPrivateChannel(ChannelDto.CreatePrivateRequest request) {
    Channel channel = request.toEntity();
    channelRepository.save(channel);
    if (request.participantIds() != null && !request.participantIds().isEmpty()) {
      List<User> participants = userRepository.findAllById(request.participantIds());

      List<ReadStatus> readStatuses = participants.stream()
          .map(user -> ReadStatus.builder()
              .user(user)
              .channel(channel)
              .lastReadAt(channel.getCreatedAt())
              .build())
          .toList();

      readStatusRepository.saveAll(readStatuses);
    }
    return channelMapper.toDto(channel);
  }

  @Override
  public ChannelDto.Response findById(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    return channelMapper.toDto(channel);
  }

  @Override
  public List<ChannelDto.Response> findAllByUserId(UUID userId) {
    List<UUID> myChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(readStatus -> readStatus.getChannel().getId())
        .toList();

    return channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, myChannelIds)
        .stream()
        .map(channelMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ChannelDto.Response update(UUID id, ChannelDto.UpdateRequest request) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new BusinessException(ErrorCode.PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED);
    }

    channel.update(request.newName(), request.newDescription());
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!channelRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.CHANNEL_NOT_FOUND);
    }
    messageRepository.deleteByChannelId(id);
    readStatusRepository.deleteByChannelId(id);

    channelRepository.deleteById(id);
  }
}