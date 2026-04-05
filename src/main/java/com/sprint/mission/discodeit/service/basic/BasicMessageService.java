package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusService userStatusService;

    @Override
    public Message create(MessageCreateRequest dto) {
        userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));
        channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));

        // binaryContent는 이미 생성된 후 이므로 가져다 쓰기만 하면 됨
        Message message = new Message(dto.userId(), dto.channelId(), dto.content(), dto.attachments());
        userStatusService.updateByUserId(dto.userId());
        return messageRepository.save(message);
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("없는 channel id 입니다."));

        return messageRepository.findAllByChannelId(channel.getId()).stream()
                .map(m -> new MessageResponse(m.getId(), m.getUserId(), m.getChannelId(),
                            m.getContent(), m.getAttachmentIds(), m.getCreatedAt()))
                .toList();
    }

    @Override
    public MessageResponse update(UUID id, MessageUpdateRequest dto) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 message id 입니다."));

        message.update(dto.content());
        messageRepository.save(message);
        userStatusService.updateByUserId(message.getUserId());

        return new MessageResponse(message.getId(), message.getUserId(), message.getChannelId(),
                message.getContent(), message.getAttachmentIds(), message.getUpdatedAt());
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 message id 입니다."));

        if (!message.getAttachmentIds().isEmpty()) {
            message.getAttachmentIds()
                    .forEach(binaryContentRepository::delete);
        }

        messageRepository.delete(message.getId());
    }
}