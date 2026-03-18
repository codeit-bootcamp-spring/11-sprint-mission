package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.message.UpdateMessageRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageEditHistory;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public Message createMessage(CreateMessageRequest request) {
        // 첨부파일 저장 (선택적)
        List<UUID> attachmentIds = new ArrayList<>();
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            request.getAttachments().forEach(attachment -> {
                BinaryContent binaryContent = new BinaryContent(
                        attachment.getFileName(),
                        attachment.getSize(),
                        attachment.getContentType(),
                        attachment.getBytes()
                );
                binaryContentRepository.save(binaryContent);
                attachmentIds.add(binaryContent.getId());
            });
        }

        Message message = new Message(
                request.getContent(),
                request.getChannelId(),
                request.getUserId(),
                attachmentIds
        );
        messageRepository.save(message);
        return message;
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return messageRepository.findByChannelId(channelId);
    }

    @Override
    public void updateMessage(UUID id, UpdateMessageRequest request) {
        Message message = messageRepository.findById(id);
        if (message != null && !message.isDeleted()) {
            message.update(request.getContent());
            messageRepository.save(message);
        }
    }

    @Override
    public void deleteMessage(UUID id) {
        Message message = messageRepository.findById(id);
        if (message != null) {
            // 첨부파일 삭제
            message.getAttachmentIds().forEach(binaryContentRepository::deleteById);
            message.delete();
            messageRepository.save(message);
        }
    }

    @Override
    public Message getMessageById(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    public List<MessageEditHistory> getMessageEditHistory(UUID messageId) {
        Message message = messageRepository.findById(messageId);
        if (message == null) return new ArrayList<>();
        return new ArrayList<>(message.getEditHistories());
    }

    @Override
    public boolean isMessageDeleted(UUID messageId) {
        Message message = messageRepository.findById(messageId);
        return message != null && message.isDeleted();
    }
}