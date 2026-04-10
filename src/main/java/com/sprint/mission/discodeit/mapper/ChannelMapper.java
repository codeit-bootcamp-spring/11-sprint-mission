package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChannelMapper {

  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserMapper userMapper;

  public ChannelResponse toResponse(Channel channel) {
    return new ChannelResponse(
        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        this.readStatusRepository.findAllByChannel(channel).stream()
            .map(ReadStatus::getUser)
            .map(this.userMapper::toResponse)
            .toList(),
        this.messageRepository.findTopCreatedAtByChannelOrderByCreatedAtDesc(channel)
            .orElse(null)
    );
  }

}
