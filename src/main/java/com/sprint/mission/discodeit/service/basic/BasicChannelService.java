package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
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
        if (request == null) {
            throw new IllegalArgumentException("채널이 null입니다.");
        }
        if (request.getChannelName() == null || request.getChannelName().isBlank()) {
            throw new IllegalArgumentException("채널 명이 null이거나 blank입니다.");
        }

        boolean isDuplicate = channelRepository.readAll().stream()
                .anyMatch(c -> c.getChannelName().equals(request.getChannelName()));
        if (isDuplicate) {
            throw new IllegalArgumentException("채널명은 중복 될 수 없습니다.");
        }

        Channel channel = new Channel(request.getChannelName(), request.getChannelDescription(), PUBLIC);

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
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
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
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        if (channel.getChannelType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정이 불가합니다.");
        }
        if (request.getChannelName() == null || request.getChannelName().isBlank()) {
            throw new IllegalArgumentException("채널명이 null이거나 blank입니다.");
        }
        boolean isDuplicate = channelRepository.readAll().stream()
                .filter(c -> !c.getId().equals(request.getChannelId()))
                .filter(c -> c.getChannelName() != null)
                .anyMatch(c -> c.getChannelName().equals(request.getChannelName()));
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 존재하는 채널명입니다.");
        }
        channel.updateChannel(request.getChannelName(), request.getChannelDescription());
        channelRepository.create(channel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.read(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        messageRepository.deleteAllByChannelId(id);
        readStatusRepository.deleteByChannelId(id);
        channelRepository.delete(id);
    }
}