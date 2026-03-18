package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public Message create(MessageCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("메세지가 null입니다.");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("메세지 내용이 null이거나 blank입니다.");
        }

        if (userRepository.read(request.getAuthorId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        if (request.getReceiverId() != null && userRepository.read(request.getReceiverId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 수신자입니다.");
        }

        if (request.getChannelId() != null && channelRepository.read(request.getChannelId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        Message message = new Message(request.getContent(), request.getChannelId(), request.getAuthorId(), request.getReceiverId());
        messageRepository.create(message);

        if (request.getFileName() != null) {
            BinaryContent binaryContent = BinaryContent.forMessage(message.getId(), request.getFileName(), request.getFileContent(), request.getContentType());
            binaryContentRepository.create(binaryContent);
        }
        return message;
    }

    @Override
    public Message read(UUID id) {
        Message message = messageRepository.read(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지입니다.");
        }
        return message;
    }

    @Override
    public List<Message> readAllByChannelId(UUID channelId) {
        return messageRepository.readAllByChannelId(channelId);
    }

    @Override
    public void update(MessageUpdateRequest request) {
        Message message = messageRepository.read(request.getMessageId());
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지입니다.");
        }
        if (request.getMessageContent() == null) {
            throw new IllegalArgumentException("메세지 내용이 null입니다.");
        }
        if (request.getMessageContent().isBlank()) {
            throw new IllegalArgumentException("메세지 내용이 blank입니다.");
        }
        message.updateContent(request.getMessageContent());
        messageRepository.create(message);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.read(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지입니다.");
        }
        binaryContentRepository.deleteByMessageId(id);
        messageRepository.delete(id);
    }
}