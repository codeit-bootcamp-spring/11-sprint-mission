package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateParam;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageDto create(MessageCreateRequest request) {
        if (userRepository.findById(request.authorId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (channelRepository.findById(request.channelId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        List<UUID> attachmentIds = new ArrayList<>();

        if (request.attachments() != null) {
            for (BinaryContentCreateRequest attachmentRequest : request.attachments()) {
                BinaryContent attachment = new BinaryContent(
                        attachmentRequest.fileName(),
                        attachmentRequest.contentType(),
                        attachmentRequest.bytes()
                );

                BinaryContent savedAttachment = binaryContentRepository.save(attachment);
                attachmentIds.add(savedAttachment.getId());
            }
        }

        Message message = new Message(
                request.authorId(),
                request.channelId(),
                request.content()
        );

        message.updateAttachments(attachmentIds);

        Message savedMessage = messageRepository.save(message);
        return toDto(savedMessage);
    }

    @Override
    public Optional<MessageDto> find(UUID id) {
        Optional<Message> optionalMessage = messageRepository.findById(id);

        if (optionalMessage.isEmpty()) {
            return Optional.empty();
        }

        Message message = optionalMessage.get();
        return Optional.of(toDto(message));
    }

    @Override
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        return messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .map(this::toDto)
                .toList();
    }

    @Override
    public MessageDto update(MessageUpdateParam param) {
        Message message = messageRepository.findById(param.id())
                .orElseThrow(() -> new IllegalArgumentException("해당 메시지가 존재하지 않습니다."));

        message.update(param.request().newContent());

        Message savedMessage = messageRepository.save(message);
        return toDto(savedMessage);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메시지가 존재하지 않습니다."));

        for (UUID attachmentId : message.getAttachmentIds()) {
            binaryContentRepository.delete(attachmentId);
        }

        messageRepository.delete(id);
    }

    private MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getUserId(),
                message.getChannelId(),
                message.getContent(),
                message.getAttachmentIds(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}