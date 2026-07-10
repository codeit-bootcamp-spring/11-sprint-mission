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
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageAttachmentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageAttachmentRepository messageAttachmentRepository;
  private final ApplicationEventPublisher eventPublisher;

  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto createMessage(CreateMessageRequest request,
      List<MultipartFile> attachments) {
    log.debug("메시지 생성 비즈니스 로직 시작 - channelId: {}, authorId: {}", request.channelId(),
        request.authorId());
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> {
          log.warn("메세지 생성 실패 - 채널이 존재하지 않음 - channelId: {}", request.channelId());
          return new ChannelNotFoundException(request.channelId());
        });
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> {
          log.warn("메시지 생성 실패 - 유저를 찾을 수 없음 - authorId: {}", request.authorId());
          return new UserNotFoundException(request.authorId());
        });

    Message message = new Message(channel, author, request.content());

    if (attachments != null && !attachments.isEmpty()) {
      log.debug("메시지 첨부파일 처리 시작 (파일 개수: {})", attachments.size());
      for (MultipartFile file : attachments) {
        try {
          log.debug("첨부파일 저장 중 - filename: {}", file.getOriginalFilename());
          String originalFileName = file.getOriginalFilename();
          String safeFileName = (originalFileName != null && !originalFileName.trim().isEmpty())
              ? originalFileName : "unnamed_attachment";

          BinaryContent content = new BinaryContent(
              safeFileName,
              file.getContentType(),
              file.getSize()
          );
          BinaryContent savedContent = binaryContentRepository.save(content);
          eventPublisher.publishEvent(
              new BinaryContentCreatedEvent(savedContent.getId(), file.getBytes()));
          message.addAttachment(savedContent);
        } catch (IOException e) {
          log.error("첨부파일 저장 중 서버 오류 발생 - filename: {}", file.getOriginalFilename(), e);
          throw new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR, Map.of(
              "fileName", file.getOriginalFilename(),
              "errorMessage", e.getMessage() != null ? e.getMessage() : "Unknown error"
          ));
        }
      }
    }

    Message savedMessage = messageRepository.save(message);

    eventPublisher.publishEvent(new MessageCreatedEvent(
        channel.getId(),
        channel.getName(),
        author.getId(),
        author.getUsername(),
        savedMessage.getContent()
    ));

    channel.updateLastMessageAt(savedMessage.getCreatedAt());

    log.info("매시지 생성 완료 - messageId: {}, channelId: {}, authorId: {}", savedMessage.getId(),
        channel.getId(), author.getId());
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
  @PreAuthorize("@messageSecurityService.isAuthor(#id, authentication.principal.userDto.id())")
  public void deleteMessage(UUID id) {
    log.debug("메시지 삭제 비즈니스 로직 시작 - messageId: {}", id);
    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("메시지 삭제 실패 - 존재하지 않는 메시지: {}", id);
          return new MessageNotFoundException(id);
        });

    if (!mes.getMessageAttachments().isEmpty()) {
      log.debug("메시지에 연관된 첨부파일 삭제 처리 (파일 개수: {})", mes.getMessageAttachments().size());
      List<BinaryContent> contentsToDelete = mes.getMessageAttachments().stream()
          .map(MessageAttachment::getBinaryContent)
          .toList();

      messageAttachmentRepository.deleteAll(mes.getMessageAttachments());
      binaryContentRepository.deleteAll(contentsToDelete);
    }

    messageRepository.delete(mes);
    log.info("메시지 삭제 완료 - messageId: {}", id);
  }

  @Override
  @Transactional
  @PreAuthorize("@messageSecurityService.isAuthor(#id, authentication.principal.userDto.id())")
  public MessageDto updateMessage(UUID id, MessageUpdateRequest request) {
    log.debug("메시지 수정 비즈니스 로직 시작 - messageId: {}", id);
    Message mes = messageRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("메시지 업데이트 실패 - 존재하지 않는 메시지: {}", id);
          return new MessageNotFoundException(id);
        });

    mes.update(request.newContent());

    log.info("메시지 업데이트 완료 - messageId: {}", id);
    return messageMapper.toDto(mes);
  }
}
