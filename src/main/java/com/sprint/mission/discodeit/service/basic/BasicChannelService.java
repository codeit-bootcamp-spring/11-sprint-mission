package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepo;
    private final MessageRepository messageRepo;
    private final ReadStatusRepository readStatusRepo;

    public Channel createPublicChannel(PublicChannelCreateRequestDto dto) {
        Channel channel = new Channel(ChannelType.PUBLIC, dto.name(), dto.description());
        channelRepo.save(channel);
        return channel;
    }

    public Channel createPrivateChannel(PrivateChannelCreateRequestDto dto) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepo.save(channel);

        for(UUID userId : dto.userIdList()) {
            readStatusRepo.save(new ReadStatus(userId, channel.getId()));
        }

        return channel;
    }

    public ChannelResponseDto findById(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        Instant latestMessageCreatedAt = messageRepo.findAll().stream()
                .filter(p -> (p.getChannelId().equals(id)))
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if(channel.getChannelType()==ChannelType.PUBLIC) {
            return new ChannelResponseDto(id, ChannelType.PUBLIC, channel.getName(), channel.getDescription(),
                    latestMessageCreatedAt, new ArrayList<>());
        } else { // PRIVATE
            List<UUID> userIds = readStatusRepo.findAll().stream()
                    .filter(p -> (p.getChannelId().equals(id)))
                    .map(ReadStatus::getUserId)
                    .toList();
            return new ChannelResponseDto(id, ChannelType.PRIVATE, null, null,
                    latestMessageCreatedAt, userIds);
        }
    }

    public List<ChannelResponseDto> findAllByUserId(UUID id) {
        List<Channel> channelList = channelRepo.findAll();
        List<Message> messageList = messageRepo.findAll();
        List<ReadStatus> readStatusList = readStatusRepo.findAll();
        List<ChannelResponseDto> response = new ArrayList<>();

        for(Channel channel : channelList) {

            Instant latestMessageCreatedAt = messageList.stream()
                    .filter(p -> (p.getChannelId().equals(channel.getId())))
                    .map(Message::getCreatedAt)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            if(channel.getChannelType() == ChannelType.PUBLIC) {
                response.add(new ChannelResponseDto(channel.getId(), ChannelType.PUBLIC, channel.getName(),
                        channel.getDescription(), latestMessageCreatedAt, new ArrayList<>()));
            } else { // PRIVATE
                List<UUID> userIds = readStatusList.stream()
                        .filter(p -> (p.getChannelId().equals(channel.getId())))
                        .map(ReadStatus::getUserId)
                        .toList();

                if(!userIds.contains(id)) continue;

                response.add(new ChannelResponseDto(channel.getId(), ChannelType.PRIVATE, null,
                        null, latestMessageCreatedAt, userIds));
            }
        }
        return response;
    }

    public void update(ChannelUpdateRequestDto dto) {
        Channel channel = channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new ChannelNotFoundException(dto.channelId()));

        if(channel.getChannelType() == ChannelType.PRIVATE) throw new PrivateChannelUpdateNotAllowedException();

        channel.setName(dto.name());
        channel.setDescription(dto.description());
        channel.update();

        channelRepo.save(channel);
    }

    public void delete(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        List<Message> messageList = messageRepo.findAll().stream()
                .filter(p -> (p.getChannelId().equals(id)))
                .toList();
        for(Message message : messageList) {
            messageRepo.delete(message);
        }

        List<ReadStatus> readStatusList = readStatusRepo.findAll().stream()
                .filter(p -> (p.getChannelId().equals(id)))
                .toList();
        for(ReadStatus readStatus : readStatusList) {
            readStatusRepo.delete(readStatus);
        }
        channelRepo.delete(channel);
    }
}
