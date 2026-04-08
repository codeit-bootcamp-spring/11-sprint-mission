package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelMessageList;
import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdate;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public MessageResponseDto createMessage(CreateMessageRequest request,
      List<MultipartFile> attachments) {
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

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        try {
          BinaryContent content = new BinaryContent(
              file.getBytes(),
              file.getOriginalFilename(),
              file.getContentType()
          );
          binaryContentRepository.save(content);
          message.getAttachmentIds().add(content.getId());
        } catch (IOException e) {
          throw new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
      }
    }

    Message savedMessage = messageRepository.save(message);

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
