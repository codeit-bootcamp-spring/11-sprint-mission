package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public ChannelResponseDto createPublic(PublicChannelCreateRequestDto dto) {
        Channel channel = new Channel(dto.getName(), dto.getDescription(), false);
        channelRepository.save(channel);
        return toResponseDto(channel, null, null);
    }

    @Override
    public ChannelResponseDto createPrivate(PrivateChannelCreateRequestDto dto) {
        // name/description 없이, PRIVATE 채널 생성
        Channel channel = new Channel(null, null, true);
        channelRepository.save(channel);

        // 참여자별 ReadStatus 생성
        for (UUID userId : dto.getParticipantUserIds()) {
            if (!userRepository.findById(userId).isPresent()) {
                throw new IllegalArgumentException(("User does not exist: " + userId));
            }
            ReadStatus readStatus = new ReadStatus(userId, channel.getId(), Instant.now());
            readStatusRepository.save(readStatus);
        }
        return toResponseDto(channel, dto.getParticipantUserIds(), null);
    }

    @Override
    public ChannelResponseDto findById(UUID id) {
        Channel channel = channelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("채널 없음"));
        // 최신 메시지 시간
        Instant lastMsgAt = messageRepository.findLatestCreatedAtByChannelId(channel.getId()).orElse(null);

        // Private 이면 참여자 목록
        List<UUID> participants = null;
        if (channel.isPrivate()) {

            //ReadStatus에서 참여자 추출
            participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                    .map(ReadStatus::getUserId)
                    .toList();
        }
        return toResponseDto(channel, participants, lastMsgAt);
    }

    @Override
    public List<ChannelResponseDto> findAllByUserId(UUID userId) {
        List<Channel> publics = channelRepository.findAllByIsPrivate(false);
        // public은 모두에게 열림
        List<Channel> privates = channelRepository.findAllPrivateByUserId(userId);
        // 본인이 차여한 PRIVATE만
        List<ChannelResponseDto> result = new ArrayList<>();
        for (Channel channel : publics) {
            List<UUID> participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                    .map(ReadStatus::getUserId)
                    .toList();
            Instant lastMsgAt = messageRepository.findLatestCreatedAtByChannelId(channel.getId()).orElse(null);
            result.add(toResponseDto(channel, participants, lastMsgAt));
        }
        return result;
    }

    @Override
    public ChannelResponseDto update(UUID channelId, ChannelUpdateRequestDto dto) {
        Channel channel = channelRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("채널없음"));
        if (channel.isPrivate()) {
            // Private 채널 수정 금지
            throw new UnsupportedOperationException("PRIVATE 채널은 수정할 수 없습니다.");
        }
        channel.update(dto.getName(), dto.getDescription());
        channelRepository.save(channel);
        return toResponseDto(channel, null, null);
    }

    @Override
    public void delete(UUID id) {
        // 메시지, ReadStatus, 채널 순서로 삭제
        messageRepository.deleteAllByChannelId(id);
        readStatusRepository.deleteAllByChannelId(id);
        channelRepository.deleteById(id);
    }

    // 변환 유틸
    private ChannelResponseDto toResponseDto(Channel ch, List<UUID> participants, Instant lastMsgAt) {
        ChannelResponseDto dto = new ChannelResponseDto();
        dto.setId(ch.getId());
        dto.setName(ch.getName());
        dto.setDescription(ch.getDescription());
        dto.setPrivate(ch.isPrivate());
        dto.setParticipantUserIds(participants);
        dto.setLatesMessageCreatedAt(lastMsgAt);
        return dto;
    }
}
