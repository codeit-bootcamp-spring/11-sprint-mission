package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepo;
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest dto) {
        Channel channel = new Channel(ChannelType.PUBLIC, dto.name(), dto.description());
        channelRepo.save(channel);
        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest dto) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepo.save(channel);

        for(UUID userId : dto.participantIds()) {
            User user = userRepo.findById(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            readStatusRepo.save(new ReadStatus(user, channel, Instant.now()));
        }

        return channelMapper.toDto(channel);
    }

    @Override
    public ChannelDto findById(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        Instant lastMessageAt = messageRepo.findLastMessageAtByChannel(channel)
                .orElse(null);

        return channelMapper.toDto(channel);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Channel> publicChannels = channelRepo.findAllByChannelType(ChannelType.PUBLIC);
        List<Channel> privateChannels = channelRepo.findChannelsByUser(user);
        List<ChannelDto> response = new ArrayList<>();

        // n+1 문제 해결 필요
        for(Channel channel : publicChannels) {
            response.add(channelMapper.toDto(channel));
        }
        for(Channel channel : privateChannels) {
            response.add(channelMapper.toDto(channel));
        }

        return response;
    }

    @Override
    @Transactional
    public void update(UUID id, ChannelUpdateRequest dto) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        if(channel.getChannelType() == ChannelType.PRIVATE)
            throw new BusinessException(ErrorCode.PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED);

        channel.update(dto.newName(), dto.newDescription());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        List<Message> messages = messageRepo.findAllByChannel(channel);

        // n+1 문제 해결 필요
        for(Message message : messages) {
            for(BinaryContent binaryContent : message.getAttachments()) {
                binaryContentRepo.delete(binaryContent);
            }
        }

        channelRepo.delete(channel);
    }
}
