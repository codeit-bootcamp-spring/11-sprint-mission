package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageAttachment;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
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
  private final BinaryContentStorage binaryContentStorage;

  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto createMessage(CreateMessageRequest request,
      List<MultipartFile> attachments) {
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHANNEL_NOT_FOUND));
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

    Message message = new Message(channel, author, request.content());

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        try {
          BinaryContent content = new BinaryContent(
              file.getOriginalFilename(),
              file.getContentType(),
              file.getSize()
          );
          BinaryContent savedContent = binaryContentRepository.save(content);
          binaryContentStorage.put(savedContent.getId(), file.getBytes());
          message.addAttachment(savedContent);
        } catch (IOException e) {
          throw new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
      }
    }

    Message savedMessage = messageRepository.save(message);
    channel.updateLastMessageAt(savedMessage.getCreatedAt());

    return messageMapper.toDto(savedMessage);
  }

  @Override
  public PageResponse<MessageDto> readMessagesByChannel(UUID channelId, Instant cursor, int size) {
    Limit limit = Limit.of(size + 1);

    List<Message> messages = (cursor == null)
        ? messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, limit)
        : messageRepository.findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(channelId, cursor,
            limit);

    boolean hasNext = messages.size() > size;

    List<MessageDto> messageDtoS = messages.stream()
        .map(messageMapper::toDto)
        .toList();

    Instant nextCursor = hasNext ? messages.get(messages.size() - 1).getCreatedAt() : null;

    return new PageResponse<>(
        messageDtoS,
        nextCursor,
        size,
        hasNext,
        null
    );
  }

  @Override
  @Transactional
  public void deleteMessage(UUID id) {
    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));

    if (!mes.getMessageAttachments().isEmpty()) {
      List<BinaryContent> contentsToDelete = mes.getMessageAttachments().stream()
          .map(MessageAttachment::getBinaryContent)
          .toList();

      binaryContentRepository.deleteAll(contentsToDelete);
    }

    messageRepository.delete(mes);
  }

  @Override
  @Transactional
  public MessageDto updateMessage(UUID id, MessageUpdateRequest request) {
    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MESSAGE_NOT_FOUND));

    mes.update(request.newContent());

    return messageMapper.toDto(mes);
  }
}
