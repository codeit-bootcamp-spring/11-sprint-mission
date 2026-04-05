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

import java.time.Instant;
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
    public Channel createPublicChannel(UUID requestUserId, PublicChannelCreateRequest request) {
        Channel newChannel = Channel.create(ChannelType.PUBLIC, request.name(), request.description(), requestUserId);
        Channel savedChannel = channelRepository.save(newChannel);
        setupUserInChannel(requestUserId, savedChannel.getId(), UserChannelRole.MASTER);
        return savedChannel;
    }

    @Override
    public Channel createPrivateChannel(UUID requestUserId, PrivateChannelCreateRequest request) {
        Channel newChannel = Channel.create(ChannelType.PRIVATE, null, null, requestUserId);
        Channel savedChannel = channelRepository.save(newChannel);

        Set<UUID> participants = new HashSet<>(request.participantIds() != null ? request.participantIds() : Collections.emptySet());
        participants.add(requestUserId); // 방장 추가

        for (UUID userId : participants) {
            userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            UserChannelRole role = userId.equals(requestUserId) ? UserChannelRole.MASTER : UserChannelRole.NORMAL;
            setupUserInChannel(userId, savedChannel.getId(), role);
        }

        return savedChannel;
    }

    @Override
    public Channel updateChannel(UUID requestUserId, UUID channelId, PublicChannelUpdateRequest request) {
        Channel channel = getChannel(channelId);

        if (!channel.isMaster(requestUserId))
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        if (channel.isPrivate())
            throw new BusinessException(ErrorCode.PRIVATE_NOT_UPDATE);

        channel.updateInfo(request.newName(), request.newDescription(), requestUserId);

        return channelRepository.save(channel);
    }

    @Override
    public void deleteChannel(UUID requestUserId, UUID channelId) {
        Channel channel = getChannel(channelId);

        if (!channel.isMaster(requestUserId))
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);

        // 유저 - 채널 관계 삭제
        userChannelRepository.findAllByChannelId(channelId)
                .forEach(uc -> userChannelRepository.deleteById(uc.getId()));

        // 메세지 삭제 - 첨부파일 삭제
        messageRepository.findAllByChannelId(channelId)
                .forEach(m -> {
                    m.getAttachmentIds().forEach(binaryContentService::delete);
                    messageRepository.deleteById(m.getId());
                });

        // 읽음 상태 삭제
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
                // Private 채널이거나 내가 참여중인 채널은 참여자 목록도 함께 보여줌
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


    // 유틸
    private void setupUserInChannel(UUID userId, UUID channelId, UserChannelRole userChannelRole) {
        UserChannel masterMapping = UserChannel.create(userId, channelId, userChannelRole);
        userChannelRepository.save(masterMapping);

        ReadStatus readStatus = ReadStatus.create(userId, channelId);
        readStatusRepository.save(readStatus);
    }

    private Channel getChannel(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
        return channel;
    }

}
