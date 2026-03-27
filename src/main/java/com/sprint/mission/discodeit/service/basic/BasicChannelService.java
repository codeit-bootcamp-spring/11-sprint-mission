package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        return new ChannelDto(channel.getId(), ChannelType.PUBLIC, channel.getName(), channel.getDescription(),
                null, new ArrayList<>());
    }

    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepo.save(channel);

        for(UUID userId : dto.userIdList()) {
            if(readStatusRepo.findByUserIdAndChannelId(userId, channel.getId()).isPresent()) {
                throw new ReadStatusAlreadyExistsException(userId, channel.getId());
            }
            readStatusRepo.save(new ReadStatus(userId, channel.getId(), Instant.now()));
        }

        return new ChannelDto(channel.getId(), ChannelType.PRIVATE, null, null,
                null, dto.userIdList());
    }

    public ChannelDto findById(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        Instant latestMessageCreatedAt = messageRepo.findLatestCreatedAtByChannelId(channel.getId())
                .orElse(null);

        if(channel.getChannelType()==ChannelType.PUBLIC) {
            return new ChannelDto(id, ChannelType.PUBLIC, channel.getName(), channel.getDescription(),
                    latestMessageCreatedAt, new ArrayList<>());
        } else { // PRIVATE
            List<UUID> userIds = readStatusRepo.findUserIdsByChannelId(id);
            return new ChannelDto(id, ChannelType.PRIVATE, null, null,
                    latestMessageCreatedAt, userIds);
        }
    }

    public List<ChannelDto> findAllByUserId(UUID id) {
        List<Channel> channelList = channelRepo.findAll();
        List<ChannelDto> response = new ArrayList<>();

        for(Channel channel : channelList) {

            Instant latestMessageCreatedAt = messageRepo.findLatestCreatedAtByChannelId(channel.getId())
                            .orElse(null);

            if(channel.getChannelType() == ChannelType.PUBLIC) {
                response.add(new ChannelDto(channel.getId(), ChannelType.PUBLIC, channel.getName(),
                        channel.getDescription(), latestMessageCreatedAt, new ArrayList<>()));
            } else { // PRIVATE
                List<UUID> userIds = readStatusRepo.findUserIdsByChannelId(channel.getId());
                if(!userIds.contains(id)) continue;

                response.add(new ChannelDto(channel.getId(), ChannelType.PRIVATE, null,
                        null, latestMessageCreatedAt, userIds));
            }
        }
        return response;
    }

    public void update(UUID id, ChannelUpdateRequest dto) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

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
            for(UUID binaryContentId : message.getAttachmentIds()) {
                BinaryContent binaryContent = binaryContentRepo.findById(binaryContentId)
                        .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));
                binaryContentRepo.delete(binaryContent);
            }
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
