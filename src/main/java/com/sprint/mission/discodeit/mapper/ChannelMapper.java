package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channeldto.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

  private final JPAMessageRepository messageRepository;
  private final JPAReadStatusRepository readStatusRepository;
  private final UserMapper userMapper;

  public ChannelDto toDto(Channel channel) {

    return new ChannelDto(

        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
            .map(readStatus -> userMapper.toDto(readStatus.getUser())).toList(),
        messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(channel.getId()).map(
            BaseEntity::getCreatedAt).orElse(Instant.now().minusSeconds(1))
    );
  }

}
