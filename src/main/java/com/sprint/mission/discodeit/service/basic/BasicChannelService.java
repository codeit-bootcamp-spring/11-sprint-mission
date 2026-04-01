package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
        channelRepository.save(channel);
        return toDto(channel);
    }

    @Override
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepository.save(channel);
        if (request.userIds() != null) {
            for (UUID userId : request.userIds()) {
                readStatusRepository.save(new ReadStatus(userId, channel.getId()));
            }
        }
        return toDto(channel);
    }

    @Override
    public ChannelDto findById(UUID id) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }
        return toDto(channel);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        List<UUID> userPrivateChannelIds = readStatusRepository.findByUserId(userId).
                stream().map(ReadStatus::getChannelId).toList();
        return channelRepository.findAll().stream().filter(
                channel ->
                        channel.getType() == ChannelType.PUBLIC ||
                                userPrivateChannelIds.contains(channel.getId())
        ).map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void update(UUID id, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }
        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("프라이빗 채널을 수정할 수 없습니다.");
        }
        channel.update(request.name());
        channelRepository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }
        readStatusRepository.deleteByChannelId(id);
        messageRepository.deleteByChannelId(id);
        channelRepository.delete(id);
    }

    private ChannelDto toDto(Channel channel) {
        Instant lastMessageAt = messageRepository.findByChannelId(channel.getId()).
                stream().map(Message::getCreatedAt).max(Instant::compareTo).
                orElse(null);
        List<UUID> participantIds = null;
        if (channel.getType() == ChannelType.PRIVATE) {
            participantIds = readStatusRepository.findByChannelId(channel.getId()).
                    stream().map(ReadStatus::getUserId).toList();
        }
        return new ChannelDto(channel.getId(), channel.getType(), channel.getName(),
                channel.getDescription(), channel.getCreatedAt(), channel.getUpdatedAt(),
                lastMessageAt, participantIds);
    }
}
