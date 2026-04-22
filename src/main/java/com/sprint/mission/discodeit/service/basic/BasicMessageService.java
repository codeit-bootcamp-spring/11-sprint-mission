package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final UserStatusRepository userStatusRepository;
  private final MessageMapper messageMapper;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest request, List<MultipartFile> attachments) {
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Channel with id " + request.channelId() + " not found"));
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Author with id " + request.authorId() + " not found"));

    Message message = new Message(request.content(), channel, author);

    if (attachments != null) {
      attachments.forEach(file -> {
        try {
          BinaryContent bc = new BinaryContent(
              file.getOriginalFilename(),
              file.getSize(),
              file.getContentType()
          );
          binaryContentRepository.save(bc);
          binaryContentStorage.put(bc.getId(), file.getBytes());
          message.addAttachment(bc);
        } catch (Exception e) {
          throw new RuntimeException("파일 저장 실패", e);
        }
      });
    }

    messageRepository.saveAndFlush(message);
    UserStatus status = userStatusRepository.findByUserId(author.getId()).orElse(null);
    UserDto authorDto = userMapper.toDto(author, status);  // ← 추가
    return messageMapper.toDto(message, status, authorDto);  // ← 수정
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, int size) {
    Pageable pageable = PageRequest.of(0, size);
    Slice<Message> slice;

    if (cursor == null) {
      slice = messageRepository.findAllByChannelIdOrderByCreatedAtDesc(channelId, pageable);
    } else {
      slice = messageRepository.findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          channelId, cursor, pageable);
    }

    List<Message> messages = slice.getContent();

    List<UUID> authorIds = messages.stream()
        .filter(m -> m.getAuthor() != null)
        .map(m -> m.getAuthor().getId())
        .distinct()
        .toList();

    Map<UUID, UserStatus> statusMap = userStatusRepository
        .findAllByUserIdIn(authorIds).stream()
        .collect(Collectors.toMap(
            us -> us.getUser().getId(),
            us -> us
        ));

    List<MessageDto> content = messages.stream()
        .map(m -> toDto(m, statusMap))
        .toList();

    Instant nextCursor = null;
    if (slice.hasNext() && !content.isEmpty()) {
      nextCursor = content.get(content.size() - 1).createdAt();
    }

    return new PageResponse<>(content, nextCursor, size, slice.hasNext(), null);
  }

  @Override
  @Transactional
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Message with id " + messageId + " not found"));
    message.updateContent(request.newContent());
    UserStatus status = message.getAuthor() != null
        ? userStatusRepository.findByUserId(message.getAuthor().getId()).orElse(null)
        : null;
    UserDto authorDto = message.getAuthor() != null  // ← 추가
        ? userMapper.toDto(message.getAuthor(), status)
        : null;
    return messageMapper.toDto(message, status, authorDto);  // ← 수정
  }

  @Override
  @Transactional
  public void delete(UUID messageId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Message with id " + messageId + " not found"));
    messageRepository.delete(message);
  }

  private MessageDto toDto(Message message, Map<UUID, UserStatus> statusMap) {
    UserStatus status = message.getAuthor() != null
        ? statusMap.get(message.getAuthor().getId())
        : null;
    UserDto authorDto = message.getAuthor() != null  // ← 추가
        ? userMapper.toDto(message.getAuthor(), status)
        : null;
    return messageMapper.toDto(message, status, authorDto);  // ← 수정
  }
}