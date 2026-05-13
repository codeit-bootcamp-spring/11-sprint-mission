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
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    log.debug("메시지 생성 요청 - channelId: {}, authorId: {}", request.channelId(), request.authorId());

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> {
          log.warn("메시지 생성 실패 - 존재하지 않는 channelId: {}", request.channelId());
          return new ChannelNotFoundException(request.channelId());
        });
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> {
          log.warn("메시지 생성 실패 - 존재하지 않는 authorId: {}", request.authorId());
          return new UserNotFoundException(request.authorId());
        });

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
          log.debug("첨부파일 저장 완료 - fileId: {}, fileName: {}", bc.getId(), bc.getFileName());
        } catch (Exception e) {
          log.error("첨부파일 저장 실패 - fileName: {}", file.getOriginalFilename(), e);
          throw new RuntimeException("파일 저장 실패", e);
        }
      });
    }

    messageRepository.saveAndFlush(message);
    log.info("메시지 생성 완료 - id: {}, channelId: {}", message.getId(), channel.getId());

    UserStatus status = userStatusRepository.findByUserId(author.getId()).orElse(null);
    UserDto authorDto = userMapper.toDto(author, status);
    return messageMapper.toDto(message, status, authorDto);
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, int size) {
    log.debug("채널별 메시지 목록 조회 - channelId: {}, cursor: {}, size: {}", channelId, cursor, size);
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
    log.debug("메시지 수정 요청 - id: {}", messageId);
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn("메시지 수정 실패 - 존재하지 않는 id: {}", messageId);
          return new MessageNotFoundException(messageId);
        });
    message.updateContent(request.newContent());
    log.info("메시지 수정 완료 - id: {}", messageId);

    UserStatus status = message.getAuthor() != null
        ? userStatusRepository.findByUserId(message.getAuthor().getId()).orElse(null)
        : null;
    UserDto authorDto = message.getAuthor() != null
        ? userMapper.toDto(message.getAuthor(), status)
        : null;
    return messageMapper.toDto(message, status, authorDto);
  }

  @Override
  @Transactional
  public void delete(UUID messageId) {
    log.debug("메시지 삭제 요청 - id: {}", messageId);
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn("메시지 삭제 실패 - 존재하지 않는 id: {}", messageId);
          return new MessageNotFoundException(messageId);
        });
    messageRepository.delete(message);
    log.info("메시지 삭제 완료 - id: {}", messageId);
  }

  private MessageDto toDto(Message message, Map<UUID, UserStatus> statusMap) {
    UserStatus status = message.getAuthor() != null
        ? statusMap.get(message.getAuthor().getId())
        : null;
    UserDto authorDto = message.getAuthor() != null
        ? userMapper.toDto(message.getAuthor(), status)
        : null;
    return messageMapper.toDto(message, status, authorDto);
  }
}