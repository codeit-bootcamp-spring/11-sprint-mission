package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel) {
        Instant lastMessageAt = messageRepository.findAllByChannel_Id(channel.getId()).stream()
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UserDto> participants = readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
                .map(readStatus -> userMapper.toDto(readStatus.getUser()))
                .toList();

        return new ChannelDto(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                participants,
                lastMessageAt
        );
    }
}