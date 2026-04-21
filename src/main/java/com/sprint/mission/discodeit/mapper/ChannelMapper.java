package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel, List<User> participants, Instant lastMessageAt) {

        return new ChannelDto(
                channel.getId(),
                channel.getChannelType(),
                channel.getName(),
                channel.getDescription(),
                participants.stream()
                        .map(userMapper::toDto)
                        .toList(),
                lastMessageAt
        );

    }
}
