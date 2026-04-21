package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Override
  @Transactional
  public ChannelDto createPublic(PublicChannelCreateRequest request) {
    Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
    channelRepository.save(channel);
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel savedChannel = channelRepository.save(channel);

    if (request.participantIds() != null) {
      for (UUID userId : request.participantIds()) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        ReadStatus readStatus = new ReadStatus(user, savedChannel, savedChannel.getCreatedAt());
        savedChannel.getReadStatuses().add(readStatus);
      }
    }
    return channelMapper.toDto(channel);
  }

  @Override
  public ChannelDto findById(UUID id) {
    Channel channel = channelRepository.findById(id).
        orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
    return channelMapper.toDto(channel);
  }

  @Override
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
  public ChannelDto update(UUID id, ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(id).
        orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("프라이빗 채널을 수정할 수 없습니다.");
    }
    channel.update(request.newName(), request.newDescription());
    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    channelRepository.deleteById(id);
  }

}
