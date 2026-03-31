package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelMessageList;
import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdate;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
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
    if (request.content() == null || request.content().isBlank()) {
      throw new BusinessLogicException(ExceptionCode.MESSAGE_CONTENT_EMPTY);
    }
    Message message = Message.builder()
        .id(UUID.randomUUID())
        .channelId(request.channelId())
        .authorId(request.authorId())
        .message(request.content())
        .attachmentIds(new ArrayList<>())
        .createdAt(Instant.now())
        .build();

    Message savedMessage = messageRepository.save(message);

    System.out.println(
        "메시지 전송 완료 : [채널 ID : " + request.channelId() + ", 작성자 ID : " + request.authorId() + "]");

    return MessageResponseDto.from(savedMessage);
  }

  @Override
  public Message readMessage(UUID id) {
    return messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));
  }

  @Override
  public List<Message> readMessagesByChannel(UUID channelId) {
    return messageRepository.findAll().stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .sorted(Comparator.comparing(Message::getCreatedAt))
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
            message.getCreatedAt(),
            message.getUpdatedAt(),
            message.getAttachmentIds()
        ))
        .toList();
  }

  @Override
  public void deleteMessage(UUID id) {
    Message mes = readMessage(id);

    if (mes.getAttachmentIds() != null && !mes.getAttachmentIds().isEmpty()) {
      binaryContentRepository.deleteAllByAttachmentIds(mes.getAttachmentIds());
    }

    messageRepository.delete(mes);
  }

  @Override
  public void updateMessage(UUID id, MessageUpdate request) {
    if (request.newContent() == null || request.newContent().isBlank()) {
      throw new BusinessLogicException(ExceptionCode.MESSAGE_CONTENT_EMPTY);
    }

    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));
    if (mes.getMessage().equals(request.newContent())) {
      System.out.println("변경사항이 없습니다.");
      return;
    }

    mes.update(request.newContent());
    messageRepository.save(mes);
  }
}
