package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.BaseEntity;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;

  @Override
  public ChannelDto.Response createPublicChannel(ChannelDto.CreatePublicRequest request) {
    Channel channel = request.toEntity();
    channelRepository.save(channel);

    return toResponse(channel);
  }

  @Override
  public ChannelDto.Response createPrivateChannel(ChannelDto.CreatePrivateRequest request) {
    Channel channel = request.toEntity();
    channelRepository.save(channel);

    // 채널 참여자 ReadStatus 생성
    if (channel.getMemberIds() != null) {
      channel.getMemberIds().forEach(userId -> {
        ReadStatus readStatus = ReadStatus.builder()
            .userId(userId)
            .channelId(channel.getId())
            .build();
        readStatusRepository.save(readStatus);
      });
    }

    return toResponse(channel);
  }

  // 공통 로직
  private ChannelDto.Response toResponse(Channel channel) {
    // 가장 최신 메시지
    Instant lastMessageAt = messageRepository.findAll().stream()
        .filter(m -> m.getChannelId().equals(channel.getId()))
        .map(BaseEntity::getCreatedAt)
        .max(Instant::compareTo)
        .orElse(null);

    // 채널 참여 멤버 목록
    List<UUID> userIds = (channel.getType() == ChannelType.PRIVATE)
        ? channel.getMemberIds()
        : null;

    return ChannelDto.Response.of(channel, lastMessageAt, userIds);
  }

  @Override
  public ChannelDto.Response findById(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    return toResponse(channel);
  }

  @Override
  public List<ChannelDto.Response> findAllByUserId(UUID userId) {
    List<UUID> myChannelIds = readStatusRepository.findAll().stream()
        .filter(rs -> rs.getUserId().equals(userId))
        .map(ReadStatus::getChannelId)
        .toList();

    return channelRepository.findAll().stream()
        .filter(channel -> channel.getType() == ChannelType.PUBLIC || myChannelIds.contains(
            channel.getId()))
        .map(this::toResponse)
        .toList();
  }

  @Override
  public ChannelDto.Response update(UUID id, ChannelDto.UpdateRequest request) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new BusinessException(ErrorCode.PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED);
    }

    channel.update(request.newName(), request.newDescription());
    return toResponse(channel);
  }

  @Override
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    // 채널 내 메시지 삭제
    messageRepository.findAll().stream()
        .filter(m -> m.getChannelId().equals(id))
        .toList()
        .forEach(m -> messageRepository.deleteById(m.getId()));

    // 채널 내 ReadStatus 삭제
    readStatusRepository.findAll().stream()
        .filter(rs -> rs.getChannelId().equals(id))
        .toList()
        .forEach(rs -> readStatusRepository.deleteById(rs.getId()));

    channelRepository.deleteById(id);
  }
}