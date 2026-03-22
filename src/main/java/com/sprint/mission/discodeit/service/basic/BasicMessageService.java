package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse createMessage(MessageCreateRequest messageCreateRequest, List<BinaryContentCreateRequest> binaryContentCreateRequests) {
        if (messageCreateRequest.content() == null || messageCreateRequest.content().isBlank())
            throw new IllegalArgumentException("content is required. ❌");

        User sender = this.userRepository.findById(messageCreateRequest.senderId())
                .orElseThrow(() -> new IllegalArgumentException("requested sender not found. ❌"));
        Channel channel = this.channelRepository.findById(messageCreateRequest.channelId())
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));

        if (!sender.getChannels().stream().anyMatch(ch -> ch.getId().equals(channel.getId()))) {
            throw new IllegalArgumentException("sender cannot send message without channel participation. ❌");
        }

        List<BinaryContent> attachments = new ArrayList<>();
        if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
            attachments = binaryContentCreateRequests.stream()
                    .map(binaryContentCreateRequest -> {
                        BinaryContent attachment = new BinaryContent(binaryContentCreateRequest);
                        this.binaryContentRepository.save(attachment);
                        return attachment;
                    })
                    .toList();
        }

        Message message = new Message(messageCreateRequest, sender, channel, attachments);
        this.messageRepository.save(message);

        sender.addMessage(message);
        this.userRepository.save(sender);

        channel.addMessage(message);
        this.channelRepository.save(channel);

        log.info("Message has been created successfully. ✅ [ID: {}]", message.getId());
        log.info("-> {channel: {}, sender: {}, content: {}}", channel.isPrivate() ? '-' : channel.getName(), sender.getNickname(), message.getContent());
        return message.toResponse(attachments);
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        Channel channel = this.channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("requested channel not found. ❌"));

        return this.messageRepository.findAll().stream()
                .filter(message -> message.getChannel().getId().equals(channel.getId()))
                .map(message -> message.toResponse(this.binaryContentRepository.findAllByIdIn(message.getAttachments())))
                .toList();
    }

    @Override
    public MessageResponse updateMessage(UUID id, MessageUpdateRequest messageUpdateRequest, List<BinaryContentCreateRequest> binaryContentCreateRequests) {
        Message message = this.messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested message not found. ❌"));

        if (messageUpdateRequest.content() != null && !messageUpdateRequest.content().isBlank())
            message.updateContent(messageUpdateRequest.content());

        List<BinaryContent> newAttachments = new ArrayList<>();
        if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
            this.binaryContentRepository.deleteAllByIdIn(message.getAttachments());
            newAttachments = binaryContentCreateRequests.stream()
                    .map(binaryContentCreateRequest -> {
                        BinaryContent attachment = new BinaryContent(binaryContentCreateRequest);
                        this.binaryContentRepository.save(attachment);
                        return attachment;
                    })
                    .toList();
        }
        if (!newAttachments.isEmpty()) message.replaceAttachments(newAttachments);

        this.messageRepository.save(message);

        log.info("Message has been updated successfully. ✅ [ID: {}]", id);
        return message.toResponse(this.binaryContentRepository.findAllByIdIn(message.getAttachments()));
    }

    @Override
    public void deleteMessage(UUID id) {
        Message message = this.messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested message not found. ❌"));

        User sender = message.getSender();
        sender.getMessages().removeIf(m -> m.getId().equals(message.getId()));
        this.userRepository.save(sender);

        Channel channel = message.getChannel();
        channel.getMessages().removeIf(m -> m.getId().equals(message.getId()));
        this.channelRepository.save(channel);

        this.binaryContentRepository.deleteAllByIdIn(message.getAttachments());

        this.messageRepository.delete(message);

        log.info("Message has been deleted successfully. ✅ [ID: {}]", id);
    }
}
