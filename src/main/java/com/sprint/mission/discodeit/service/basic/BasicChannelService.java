package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.ChannelReadDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
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

@Service
@RequiredArgsConstructor // 생성자
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    // Create
//    @Override
//    public Channel create(Channel.ChannelType channelType, String name, String description) {
//        Channel channel = Channel.create(channelType, name, description);
//        channelRepository.insert(channel);
//        System.out.println("채널을 생성하였습니다.");
//        System.out.println();
//
//        return channel;
//    }
    @Override
    public Channel createPublic(ChannelCreatePublicRequest dto) {
        Channel channel = Channel.createPublic(dto.name(), dto.description());
        channelRepository.insert(channel);

        return channel;
    }

    @Override
    public Channel createPrivate(ChannelCreatePrivateRequest dto) {
        Channel channel = Channel.createPrivate();
        channelRepository.insert(channel);

        // private 채널 참여자들의 ReadStatus 생성
        // readStatusService를 사용하면 같은 레이어(여기서는 Service)간에 순환 참조가 생기므로 readStatusService.create 사용 X
        dto.participantIds().stream()
                .map(userId -> new ReadStatus(userId, channel.getId(), Instant.now()))
                .forEach(readStatusRepository::insert);

        return channel;
    }


    // Read
    @Override
    public ChannelReadDto find(UUID id) {
        Channel channel = channelRepository.findById(id);
        System.out.println();

        // 가장 최근 메시지 시간을 조회
        // createdAt 기준으로 내림차순 정렬
        List<Message> messages = messageRepository.findAllByChannelId(id);
        messages.sort(Comparator.comparing(Message::getCreatedAt).reversed()); // 내림차순으로 특정 채널 내 메세지들 정렬
        Instant lastMessageAt = messages.isEmpty() ? null : messages.get(0).getCreatedAt(); // 가장 최근 메시지의 시간

        // PRIVATE 채널일 경우 참여자 포함
        List<UUID> participants = List.of();
        if (channel.getType() == Channel.Type.PRIVATE) {
            participants = readStatusRepository.findByChannelId(id).stream()
                    .map(ReadStatus::getUserId)
                    .toList();
        }

        return new ChannelReadDto(
                channel.getId(),
                channel.getCreatedAt(),
                channel.getUpdatedAt(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                participants,
                lastMessageAt
        );
    }

    @Override
    public List<ChannelReadDto> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(channel -> { // PUBLIC이면 전체 유저가 채널 조회 가능, PRIVATE는 해당 USER가 참여한 채널만 조회 가능
                    // PUBLIC
                    if (channel.getType() == Channel.Type.PUBLIC) {
                        return true;
                    }
                    // PRIVATE
                    return readStatusRepository.findByChannelId(channel.getId()).stream()
                            .anyMatch(readStatus -> readStatus.getUserId().equals(userId));
                })
                .map(channel -> {
                    // 최근 메시지의 시간 조회
                    List<Message> messages = messageRepository.findAllByChannelId(channel.getId());
                    messages.sort(Comparator.comparing(Message::getCreatedAt).reversed()); // 내림차순으로 특정 채널 내 메세지들 정렬
                    Instant lastMessageAt = messages.isEmpty() ? null : messages.get(0).getCreatedAt(); // 가장 최근 메시지의 시간

                    // PRIVATE 채널일 경우 참여자 포함
                    List<UUID> participants = List.of();
                    if (channel.getType() == Channel.Type.PRIVATE) {
                        participants = readStatusRepository.findByChannelId(channel.getId()).stream()
                                .map(ReadStatus::getUserId)
                                .toList();
                    }

                    return new ChannelReadDto(
                            channel.getId(),
                            channel.getCreatedAt(),
                            channel.getUpdatedAt(),
                            channel.getType(),
                            channel.getName(),
                            channel.getDescription(),
                            participants,
                            lastMessageAt
                    );
                    // 최근 메시지 시간 조회
                })
                .toList();
    }


    // Update
    @Override
    public Channel update(UUID id, ChannelUpdateRequest dto) {
        Channel channel = channelRepository.findById(id);

        // PRIVATE 채널은 수정 불가
        if (channel.getType() == Channel.Type.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        if (dto.newName() != null) {
            channel.updateName(dto.newName());
        }
        if (dto.newDescription() != null) {
            channel.updateDescription(dto.newDescription());
        }
        channelRepository.update(channel);
        System.out.println();

        return channel;
    }

    // Delete
    // 기존 채널만 삭제
    // 고도화 이후 : 채널 내 모든 메시지, 채널의 최근 메시지를 읽은 유저의 시간, 해당 채널 삭제
    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id);
        messageRepository.deleteAllByChannelId(id);
        readStatusRepository.deleteAllByChannelId(id);
        channelRepository.delete(id);
    }
}
