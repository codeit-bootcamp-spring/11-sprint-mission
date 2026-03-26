package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdate;
import com.sprint.mission.discodeit.dto.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.PublicChannelRequest;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public Channel createPublicChannel(PublicChannelRequest request) {
        Channel channel = new Channel(
                request.name(),
                request.description(),
                request.type(),
                request.ownerId()
        );
        Channel savedChannel = channelRepository.save(channel);
        System.out.println(savedChannel.getName() + " 채널이 생성되었습니다.");
        return savedChannel;
    }

    @Override
    public Channel createPrivateChannel(PrivateChannelRequest request) {
        Channel channel = Channel.builder()
                .type(ChannelType.PRIVATE)
                .ownerId(request.ownerId())
                .build();

        Channel createdChannel = channelRepository.save(channel);

        Set<UUID> allParticipants = new HashSet<>(request.participantsIds());
        allParticipants.add(request.ownerId());

        allParticipants.stream()
                .map(userId -> new ReadStatus(userId, createdChannel.getId(), Instant.MIN))
                .forEach(readStatusRepository::save);

        return createdChannel;
    }

    @Override
    public ChannelResponse readChannel(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널이 존재하지 않습니다."));

        List<UUID> memberIds = null;
        if(channel.getType() == ChannelType.PRIVATE) {
            memberIds = readStatusRepository.findAllByChannelId(id).stream()
                    .map(ReadStatus::getUserId)
                    .toList();
        }

        Instant lastMessageAt = messageRepository.findLatestMessage(id)
                .map(Message::getCreatedAt)
                .orElse(channel.getCreatedAt());

        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getType(),
                channel.getDescription(),
                channel.getOwnerId(),
                lastMessageAt,
                memberIds
        );
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        List<Channel> allChannels = channelRepository.findAll();

        Set<UUID> channelIds = readStatusRepository.findAllByChannelId(userId).stream()
                .map(ReadStatus::getChannelId)
                .collect(Collectors.toSet());

        return allChannels.stream()
                .filter(channel -> {
                    boolean isPublic = (channel.getType() == ChannelType.PUBLIC);
                    boolean isMember = channelIds.contains(channel.getId());

                    return isPublic || isMember;
                })
                .map(channel -> {
                    Instant lastMessageAt = messageRepository.findLatestMessage(channel.getId())
                            .map(Message::getCreatedAt)
                            .orElse(channel.getCreatedAt());

                    List<UUID> memberIds = Collections.emptyList();
                    if(channel.getType() == ChannelType.PRIVATE) {
                        memberIds = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                                .map(ReadStatus::getUserId)
                                .toList();
                    }
                    return new ChannelResponse(
                            channel.getId(),
                            channel.getName(),
                            channel.getType(),
                            channel.getDescription(),
                            channel.getOwnerId(),
                            lastMessageAt,
                            memberIds
                    );
                })
                .sorted(Comparator.comparing(ChannelResponse::lastMessageAt).reversed())
                .toList();
    }

    @Override
    public void deleteChannel(UUID id) {
        if(!channelRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("삭제할 채널이 존재하지 않습니다.");
        }

        messageRepository.deleteAllByChannelId(id);
        readStatusRepository.deleteAllByChannelId(id);

        channelRepository.delete(id);
    }

    @Override
    public void updateChannel(UUID id, ChannelUpdate request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다."));

        if(channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE채널은 수정할 수 없습니다.");
        }

        String name = (request.name() != null) ? request.name() : channel.getName();
        String description = (request.description() != null) ? request.description() : channel.getDescription();

        channel.update(name, description);
        channelRepository.save(channel);
    }
}
