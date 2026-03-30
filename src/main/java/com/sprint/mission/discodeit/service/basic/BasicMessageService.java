package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.binaryContent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.InvalidMessageRequestException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("service-basic")
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {


    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    @Transactional
    public UUID create(MessageCreateRequest request) {
        if (userRepository.findById(request.getSenderId()) == null) {
            throw new UserNotFoundException("메시지 생성 실패: 발송자 ID를 찾을 수 없습니다.");
        }
        if (channelRepository.findById(request.getChannelId()) == null) {
            throw new ChannelNotFoundException("메시지 생성 실패: 수신할 채널 ID를 찾을 수 없습니다.");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new InvalidMessageRequestException("메시지 내용이 비어있습니다.");
        }
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            request.getAttachmentIds().forEach(fileId -> {
                if (binaryContentRepository.findById(fileId) == null) {
                    throw new BinaryContentNotFoundException("첨부파일 생성 실패: 파일 ID(" + fileId + ")를 찾을 수 없습니다.");
                }
            });
        }

        Message message = new Message(request.getContent(), request.getSenderId(), request.getChannelId());

        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            request.getAttachmentIds().forEach(message::addAttachment);
        }

        messageRepository.save(message);
        System.out.println("메시지가 성공적으로 생성되었습니다.");
        
        return message.getId();
    }

    @Override
    @Transactional
    public Message read(UUID id) {
        Message message = messageRepository.findById(id);
        if (message == null) {
            throw new MessageNotFoundException("조회할 메시지를 찾을 수 없습니다. (ID: " + id + ")");
        }
        return message;
    }

    @Override
    @Transactional
    public List<Message> readAllByChannelId(UUID channelId) {
        return messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }

    public List<Message> readAllBySenderId(UUID userId) {
        return messageRepository.findAll().stream()
                .filter(message -> message.getSenderId().equals(userId))
                .toList();
    }

    @Override
    @Transactional
    public void update(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new MessageNotFoundException("수정할 메시지를 찾을 수 없습니다. (ID: " + messageId + ")");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new InvalidMessageRequestException("수정할 메시지 내용이 비어있습니다.");
        }

        message.update(request.getContent());
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepository.findById(id);
        if (message == null) {
            throw new MessageNotFoundException("삭제할 메시지를 찾을 수 없습니다. (ID: " + id + ")");
        }

        if (message.getAttachmentIds() != null && !message.getAttachmentIds().isEmpty()) {
            message.getAttachmentIds().forEach(binaryContentRepository::deleteById);
        }

        messageRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void clearMessagesInChannel(UUID channelId) {
        // 채널 내 모든 메시지 삭제
        List<Message> channelMessages = this.readAllByChannelId(channelId);
        channelMessages.forEach(message -> this.delete(message.getId()));
    }

    @Override
    @Transactional
    public void clearMessagesByUser(UUID userId) {
        // 유저가 쓴 모든 메시지 삭제
        List<Message> userMessages = this.readAllBySenderId(userId);
        userMessages.forEach(message -> this.delete(message.getId()));
    }

    @Override
    public void setUserService(UserService userService) {}

    @Override
    public void setChannelService(ChannelService channelService) {}
}
