package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.ChannelType.PUBLIC;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {
        if (channelRepository.existsByChannelName(request.getChannelName())) {
            throw DiscodeitException.duplicateChannel(request.getChannelName());
        }

        Channel channel = new Channel(request.getChannelName(), request.getChannelDescription(), PUBLIC);
        channel.validateService();
        channelRepository.create(channel);
        return new ChannelResponse(channel.getId(), channel.getChannelName(), channel.getChannelDescription(), null, null);


    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(null, null, ChannelType.PRIVATE);
        channelRepository.create(channel);

        request.getUserIds().forEach(userIds -> {
            ReadStatus readStatus = new ReadStatus(userIds, channel.getId(), Instant.now());
            readStatusRepository.create(readStatus);
        });

        return new ChannelResponse(channel.getId(), null, null, null, request.getUserIds());
    }

    @Override
    public ChannelResponse read(UUID id) {
        Channel channel = channelRepository.read(id);
        if (channel == null) {
            throw DiscodeitException.channelNotFound(id);
        }

        Instant lastMessageAt = messageRepository.readAllByChannelId(id).stream()
                .map(m -> m.getCreatedAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

        List<UUID> userIds = null;
        if (channel.getChannelType() == ChannelType.PRIVATE) {
            userIds = readStatusRepository.readAllByChannelId(id).stream()
                    .map(rs -> rs.getUserId())
                    .toList();
        }
        return new ChannelResponse(channel.getId(), channel.getChannelName(), channel.getChannelDescription(), lastMessageAt, userIds);
    }

    @Override
    public List<ChannelResponse> readAllByUserId(UUID userId) {
        return channelRepository.readAll().stream()
                .filter(channel -> channel.getChannelType() == ChannelType.PUBLIC ||
                        readStatusRepository.readAllByChannelId(channel.getId()).stream()
                                .anyMatch(r -> r.getUserId().equals(userId)))
                .map(channel -> {
                    Instant lastMessageAt = messageRepository.readAllByChannelId(channel.getId()).stream()
                            .map(m -> m.getCreatedAt())
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                    List<UUID> userIds = null;
                    if (channel.getChannelType() == ChannelType.PRIVATE) {
                        userIds = readStatusRepository.readAllByChannelId(channel.getId()).stream()
                                .map(r -> r.getUserId())
                                .toList();
                    }
                    return new ChannelResponse(channel.getId(), channel.getChannelName(), channel.getChannelDescription(), lastMessageAt, userIds);
                })
                .toList();
    }

    @Override
    public void update(ChannelUpdateRequest request) {
        Channel channel = channelRepository.read(request.getChannelId());
        if (channel == null) throw DiscodeitException.channelNotFound(request.getChannelId());
        if (channel.getChannelType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정이 불가합니다.");
        }
        if (channelRepository.existsByChannelNameExcluding(request.getChannelName(), request.getChannelId())) {
            throw DiscodeitException.duplicateChannel(request.getChannelName());
        }
        channel.updateChannel(request.getChannelName(), request.getChannelDescription());
        channel.validateService();
        channelRepository.create(channel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.read(id);
        if (channel == null) {
            throw DiscodeitException.channelNotFound(id);
        }
        messageRepository.deleteAllByChannelId(id);
        readStatusRepository.deleteByChannelId(id);
        channelRepository.delete(id);
    }

    @Override
    public void restore(UUID id) {
        channelRepository.restore(id);
    }
}