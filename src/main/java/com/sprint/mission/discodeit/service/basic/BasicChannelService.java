package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepo;
    private final MessageRepository messageRepo;
    private final ReadStatusRepository readStatusRepo;
    private final BinaryContentRepository binaryContentRepo;

    public ChannelDto createPublicChannel(PublicChannelCreateRequest dto) {
        Channel channel = new Channel(ChannelType.PUBLIC, dto.name(), dto.description());
        channelRepo.save(channel);
        return toDto(channel, null, List.of());
    }

    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepo.save(channel);

        for(UUID userId : dto.participantIds()) {
            if(readStatusRepo.findByUserIdAndChannelId(userId, channel.getId()).isPresent()) {
                throw new BusinessException(ErrorCode.READ_STATUS_ALREADY_EXISTS);
            }
            readStatusRepo.save(new ReadStatus(userId, channel.getId(), Instant.now()));
        }

        return toDto(channel, null, dto.participantIds());
    }

    public ChannelDto findById(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        Instant lastMessageAt = messageRepo.findLastMessageAtByChannelId(channel.getId())
                .orElse(null);

        return switch(channel.getChannelType()) {
            case PUBLIC -> toDto(channel, lastMessageAt, List.of());
            case PRIVATE -> {
                List<UUID> userIds = readStatusRepo.findUserIdsByChannelId(channel.getId());
                yield toDto(channel, lastMessageAt, userIds);
            }
        };
    }

    public List<ChannelDto> findAllByUserId(UUID id) {
        List<Channel> channelList = channelRepo.findAll();
        List<Message> messageList = messageRepo.findAll();
        List<ReadStatus> readStatusList = readStatusRepo.findAll();
        List<ChannelDto> response = new ArrayList<>();

        Map<UUID, List<Message>> messagesByChannel = messageList.stream()
                .collect(Collectors.groupingBy(Message::getChannelId));
        Map<UUID, List<ReadStatus>> readStatusesByChannel = readStatusList.stream()
                .collect(Collectors.groupingBy(ReadStatus::getChannelId));

        for(Channel channel : channelList) {
            List<Message> channelMessages = messagesByChannel.getOrDefault(channel.getId(),List.of());
            Instant lastMessageAt = channelMessages.stream()
                    .map(Message::getCreatedAt)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            switch(channel.getChannelType()) {
                case PUBLIC -> response.add(toDto(channel, lastMessageAt, List.of()));
                case PRIVATE -> {
                    List<UUID> userIds = readStatusesByChannel.getOrDefault(channel.getId(), List.of()).stream()
                            .map(ReadStatus::getUserId)
                            .toList();
                    if(!userIds.contains(id)) continue;
                    response.add(toDto(channel, lastMessageAt, userIds));
                }
            }
        }
        return response;
    }

    public void update(UUID id, ChannelUpdateRequest dto) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        if(channel.getChannelType() == ChannelType.PRIVATE)
            throw new BusinessException(ErrorCode.PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED);

        channel.setName(dto.newName());
        channel.setDescription(dto.newDescription());
        channel.update();

        channelRepo.save(channel);
    }

    public void delete(UUID id) {
        channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        List<Message> messageList = messageRepo.findAll().stream()
                .filter(p -> (p.getChannelId().equals(id)))
                .toList();
        for(Message message : messageList) {
            for(UUID binaryContentId : message.getAttachmentIds()) {
                binaryContentRepo.findById(binaryContentId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND));
                binaryContentRepo.deleteById(binaryContentId);
            }
            messageRepo.deleteById(message.getId());
        }

        List<ReadStatus> readStatusList = readStatusRepo.findAll().stream()
                .filter(p -> (p.getChannelId().equals(id)))
                .toList();
        for(ReadStatus readStatus : readStatusList) {
            readStatusRepo.deleteById(readStatus.getId());
        }

        channelRepo.deleteById(id);
    }

    private ChannelDto toDto(Channel channel, Instant lastMessageAt, List<UUID> userIds) {
        ChannelType channelType = channel.getChannelType();

        return switch (channelType) {
            case PUBLIC -> new ChannelDto(
                    channel.getId(),
                    channel.getCreatedAt(),
                    channel.getUpdatedAt(),
                    channelType,
                    channel.getName(),
                    channel.getDescription(),
                    lastMessageAt,
                    List.of()
            );
            case PRIVATE -> new ChannelDto(
                    channel.getId(),
                    channel.getCreatedAt(),
                    channel.getUpdatedAt(),
                    channelType,
                    null,
                    null,
                    lastMessageAt,
                    userIds
            );
        };
    }
}
