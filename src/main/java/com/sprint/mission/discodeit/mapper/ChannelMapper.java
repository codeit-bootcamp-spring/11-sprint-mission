package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;

// 모범 답안을 참고하여 작성했습니다.
@Mapper(componentModel = "spring", uses = UserMapper.class)
public abstract class ChannelMapper {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ReadStatusRepository readStatusRepository;

  @Autowired
  private UserMapper userMapper;

  @Mapping(target = "lastMessageAt", expression = "java(resolveLastMessageAt(channel))")
  @Mapping(target = "participants", expression = "java(resolveParticipants(channel))")
  abstract public ChannelDto toDto(Channel channel);

  protected Instant resolveLastMessageAt(Channel channel) {
    return messageRepository.findFirstByChannelIdOrderByCreatedAtDesc(
            channel.getId())
        .map(Message::getCreatedAt)
        .orElse(Instant.MIN);
  }

  protected List<UserDto> resolveParticipants(Channel channel) {
    return readStatusRepository.findAllByChannelIdWithUserWithStatusAndProfile(channel.getId())
        .stream()
        .map(rs -> userMapper.toDto(rs.getUser()))
        .toList();
  }
}
