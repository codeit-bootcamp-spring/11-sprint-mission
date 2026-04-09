package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelMessageList;
import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdate;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  @Transactional
  public MessageResponseDto createMessage(CreateMessageRequest request,
      List<MultipartFile> attachments) {
    if (request.content() == null || request.content().isBlank()) {
      throw new BusinessLogicException(ExceptionCode.MESSAGE_CONTENT_EMPTY);
    }
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

    Message message = new Message(channel, author, request.content());

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        try {
          BinaryContent content = new BinaryContent(
              file.getBytes(),
              file.getOriginalFilename(),
              file.getContentType()
          );
          binaryContentRepository.save(content);
          message.addAttachment(content);
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
    return messageRepository.findByChannelId(channelId).stream()
        .sorted(Comparator.comparing(Message::getCreatedAt))
        .collect(Collectors.toList());
  }

  @Override
  public List<MessageResponseDto> findAllByChannelId(ChannelMessageList request) {
    return messageRepository.findByChannelId(request.channelId()).stream()
        .sorted(Comparator.comparing(Message::getCreatedAt))
        .map(MessageResponseDto::from)
        .toList();
  }

  @Override
  @Transactional
  public void deleteMessage(UUID id) {
    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));

    if (!mes.getAttachments().isEmpty()) {
      binaryContentRepository.deleteAll(mes.getAttachments());
    }

    messageRepository.delete(mes);
  }

  @Override
  @Transactional //여기 하는중!
  public void updateMessage(UUID id, MessageUpdate request) {
    if (request.newContent() == null || request.newContent().isBlank()) {
      throw new BusinessLogicException(ExceptionCode.MESSAGE_CONTENT_EMPTY);
    }

    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));

    mes.update(request.newContent());
  }
}
