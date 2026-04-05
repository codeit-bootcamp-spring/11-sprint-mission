package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final UserChannelRepository userChannelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public Channel createPublicChannel(PublicChannelCreateRequest request) {
        Channel newChannel = Channel.create(ChannelType.PUBLIC, request.name(), request.description(), null);
        return channelRepository.save(newChannel);
    }

    @Override
    public Channel createPrivateChannel(PrivateChannelCreateRequest request) {
        Channel newChannel = Channel.create(ChannelType.PRIVATE, null, null, null);
        Channel savedChannel = channelRepository.save(newChannel);

        if (request.participantIds() != null) {
            for (UUID userId : request.participantIds()) {
                userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                setupUserInChannel(userId, savedChannel.getId(), UserChannelRole.NORMAL);
            }
        }

        return savedChannel;
    }

    @Override
    public Channel updateChannel(UUID channelId, PublicChannelUpdateRequest request) {
        Channel channel = getChannel(channelId);

        if (channel.isPrivate()) {
            throw new BusinessException(ErrorCode.PRIVATE_NOT_UPDATE);
        }

        if (request.newName() != null) {
            channel.updateInfo(request.newName(), request.newDescription(), null);
        }

        return channelRepository.save(channel);
    }

    @Override
    public void deleteChannel(UUID channelId) {
        Channel channel = getChannel(channelId);

        userChannelRepository.findAllByChannelId(channelId)
                .forEach(uc -> userChannelRepository.deleteById(uc.getId()));

        messageRepository.findAllByChannelId(channelId).forEach(m -> {
            if (m.getAttachmentIds() != null) {
                m.getAttachmentIds().forEach(binaryContentService::delete);
            }
            messageRepository.deleteById(m.getId());
        });

        readStatusRepository.findByChannelId(channelId)
                .forEach(rs -> readStatusRepository.deleteById(rs.getId()));

        channelRepository.deleteById(channelId);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        List<UUID> myJoinedChannelIds = userChannelRepository.findAllByUserId(userId).stream()
                .map(UserChannel::getChannelId)
                .toList();

        List<ChannelDto> result = new ArrayList<>();
        List<Channel> allChannels = channelRepository.findAll();

        for (Channel channel : allChannels) {
            boolean isVisible = channel.getType() == ChannelType.PUBLIC || myJoinedChannelIds.contains(channel.getId());

            if (isVisible) {
                List<UUID> participantIds = new ArrayList<>();
                if (channel.isPrivate() || myJoinedChannelIds.contains(channel.getId())) {
                    participantIds = userChannelRepository.findAllByChannelId(channel.getId()).stream()
                            .map(UserChannel::getUserId)
                            .toList();
                }
                result.add(ChannelDto.from(channel, participantIds));
            }
        }

        return result;
    }

    private void setupUserInChannel(UUID userId, UUID channelId, UserChannelRole role) {
        UserChannel uc = UserChannel.create(userId, channelId, role);
        userChannelRepository.save(uc);

        ReadStatus rs = ReadStatus.create(userId, channelId);
        readStatusRepository.save(rs);
    }

    private Channel getChannel(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
    }
}