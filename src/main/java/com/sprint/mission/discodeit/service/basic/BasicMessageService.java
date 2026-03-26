package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse create(MessageCreateRequest request) {
        validate(request.authorId(), request.channelId());

        Message message = new Message(
                request.authorId(),
                request.channelId(),
                request.content()
        );

        if (request.attachments() != null) {
            for (BinaryContentCreateRequest attachment : request.attachments()) {
                BinaryContent binaryContent = new BinaryContent(
                        attachment.fileName(),
                        attachment.contentType(),
                        attachment.bytes()
                );

                BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
                message.addAttachment(savedBinaryContent.getId());
            }
        }

        Message savedMessage = messageRepository.save(message);
        return MessageResponse.of(savedMessage);
    }

    @Override
    public MessageResponse findById(UUID id) {
        Message message = messageRepository.findById(id);

        if (message == null) {
            throw new IllegalStateException("존재하지 않는 메세지 입니다.");
        }

        return MessageResponse.of(message);
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        if (channelRepository.findById(channelId) == null) {
            throw new IllegalStateException("존재하지 않는 채널입니다.");
        }

        return messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .map(MessageResponse::of)
                .toList();
    }

    @Override
    public MessageResponse update(UUID id, MessageUpdateRequest request) {
        Message message = messageRepository.findById(id);

        if (message == null) {
            throw new IllegalStateException("존재하지 않는 메세지입니다.");
        }

        message.updateContent(request.content());
        Message updatedMessage = messageRepository.save(message);

        return MessageResponse.of(updatedMessage);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id);

        if (message == null) {
            throw new IllegalStateException("존재하지 않는 메세지입니다.");
        }

        for (UUID attachmentId : message.getAttachmentIds()) {
            binaryContentRepository.delete(attachmentId);
        }

        messageRepository.delete(id);
    }

    private void validate(UUID authorId, UUID channelId) {
        if (userRepository.findById(authorId) == null) {
            throw new IllegalStateException("존재하지 않는 유저입니다.");
        }

        if (channelRepository.findById(channelId) == null) {
            throw new IllegalStateException("존재하지 않는 채널입니다.");
        }
    }
}