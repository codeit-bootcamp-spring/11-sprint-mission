package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = UserMapper.class)
public abstract class ChannelMapper {

    @Autowired
    protected MessageRepository messageRepository;

    @Autowired
    protected ReadStatusRepository readStatusRepository;

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "lastMessageAt", ignore = true)
    @Mapping(target = "participants", ignore = true)
    public abstract ChannelDto toDto(Channel channel);

    @AfterMapping
    protected void enrichChannelDto(Channel channel, @MappingTarget ChannelDto.ChannelDtoBuilder builder) {
        builder.lastMessageAt(
                messageRepository.findLatestCreatedAtByChannelId(channel.getId()).orElse(null)
        );
        if (channel.getType() == ChannelType.PRIVATE) {
            List<UserDto> participants = readStatusRepository.findAllByChannelId(channel.getId())
                    .stream()
                    .map(rs -> userMapper.toDto(rs.getUser()))
                    .distinct()
                    .toList();
            builder.participants(participants);
        }
    }
}
