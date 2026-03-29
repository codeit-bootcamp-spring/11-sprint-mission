package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse createMessage(MessageCreateRequest messageCreateRequest, List<BinaryContentCreateRequest> binaryContentCreateRequests) {
        if (messageCreateRequest.content() == null || messageCreateRequest.content().isBlank())
            throw new IllegalArgumentException("content is required. ❌");

        User sender = this.userRepository.findById(messageCreateRequest.senderId())
                .orElseThrow(() -> new IllegalArgumentException("requested sender not found. ❌"));
        Channel channel = this.channelRepository.findById(messageCreateRequest.channelId())
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));

        if (channel.isPrivate() && !this.readStatusRepository.existByUserIdAndChannelId(sender.getId(), channel.getId())) {
            throw new IllegalArgumentException("sender cannot send message without channel participation. ❌");
        }

        List<BinaryContent> attachments = new ArrayList<>();
        if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
            attachments = binaryContentCreateRequests.stream()
                    .map(req -> {
                        BinaryContent attachment = new BinaryContent(req.data(), req.fileName(), req.contentType(), req.size());
                        this.binaryContentRepository.save(attachment);
                        return attachment;
                    })
                    .toList();
        }

        Message message = new Message(
                messageCreateRequest.content(),
                sender.getId(),
                channel.getId(),
                attachments.stream()
                        .map(BinaryContent::getId)
                        .toList()
        );
        this.messageRepository.save(message);

        log.info("Message has been created successfully. ✅ [ID: {}]", message.getId());
        log.info("-> {channel: {}, sender: {}, content: {}}", channel.isPrivate() ? '-' : channel.getName(), sender.getNickname(), message.getContent());
        return this.toResponse(message);
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        return this.messageRepository.findAllByChannelId(channelId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MessageResponse updateMessage(UUID id, MessageUpdateRequest messageUpdateRequest, List<BinaryContentCreateRequest> binaryContentCreateRequests) {
        Message message = this.messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested message not found. ❌"));

        if (messageUpdateRequest != null) {
            if (messageUpdateRequest.content() != null && !messageUpdateRequest.content().isBlank())
                message.updateContent(messageUpdateRequest.content());
        }

        List<BinaryContent> newAttachments = new ArrayList<>();
        if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
            this.binaryContentRepository.deleteAllByIdIn(message.getAttachmentIds());
            newAttachments = binaryContentCreateRequests.stream()
                    .map(req -> {
                        BinaryContent attachment = new BinaryContent(req.data(), req.fileName(), req.contentType(), req.size());
                        this.binaryContentRepository.save(attachment);
                        return attachment;
                    })
                    .toList();
        }
        if (!newAttachments.isEmpty()) message.replaceAttachments(newAttachments);

        this.messageRepository.save(message);

        log.info("Message has been updated successfully. ✅ [ID: {}]", id);
        return this.toResponse(message);
    }

    @Override
    public void deleteMessage(UUID id) {
        Message message = this.messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested message not found. ❌"));

        this.binaryContentRepository.deleteAllByIdIn(message.getAttachmentIds());
        this.messageRepository.delete(message);

        log.info("Message has been deleted successfully. ✅ [ID: {}]", id);
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getSenderId(),
                message.getChannelId(),
                message.getAttachmentIds()
        );
    }
}
