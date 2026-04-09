package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final MessageRepository messageRepo;
    private final ReadStatusRepository readStatusRepo;
    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel) {
        if(channel == null) return null;

        Instant lastMessageAt = messageRepo.findLastMessageAtByChannel(channel)
                .orElse(null);

        List<UserDto> participants = List.of();
        if(channel.getChannelType() == ChannelType.PRIVATE) {
            participants = readStatusRepo.findUsersByChannel(channel).stream()
                    .map(userMapper::toDto)
                    .toList();
        }

        return new ChannelDto(
                channel.getId(),
                channel.getChannelType(),
                channel.getName(),
                channel.getDescription(),
                participants,
                lastMessageAt
        );

    }
}
