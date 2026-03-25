package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelMessageList;
import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdate;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponseDto createMessage(CreateMessageRequest request) {
        if(request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("메세지 내용은 비어있을 수 없습니다.");
        }
        Message message = Message.builder()
                .channelId(request.channelId())
                .authorId(request.authorId())
                .message(request.message())
                .createdAt(Instant.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        if(request.binaryContentIds() != null && !request.binaryContentIds().isEmpty()) {
            for(UUID contentId : request.binaryContentIds()) {
                binaryContentRepository.findById(contentId).ifPresent(content -> {
                    content.setMessageId(savedMessage.getId());
                    binaryContentRepository.save(content);
                });
            }
        }

        System.out.println("메시지 전송 완료 : [채널 ID : " + request.channelId() + ", 작성자 ID : " + request.authorId() + "]" );

        return MessageResponseDto.from(savedMessage);
    }

    @Override
    public Message readMessage(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메세지가 존재하지 않습니다."));
    }

    @Override
    public List<Message> readMessagesByChannel(UUID channelId) {
        return messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageResponseDto> findAllByChannelId(ChannelMessageList request) {
        return messageRepository.findByChannelId(request.channelId()).stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .map(message -> new MessageResponseDto(
                        message.getId(),
                        message.getChannelId(),
                        message.getAuthorId(),
                        message.getMessage(),
                        message.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public void deleteMessage(UUID id) {
        Message mes = readMessage(id);

        binaryContentRepository.deleteAllByMessageId(id);

        messageRepository.delete(mes);
    }

    @Override
    public void updateMessage(MessageUpdate request) {
        if(request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("메세지 내용은 비어있을 수 없습니다.");
        }

        Message mes = messageRepository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("수정할 메시지가 존재하지 않습니다."));
        if(mes.getMessage().equals(request.message())) {
            System.out.println("변경사항이 없습니다.");
            return;
        }

        mes.update(request.message());
        messageRepository.save(mes);
    }
}
