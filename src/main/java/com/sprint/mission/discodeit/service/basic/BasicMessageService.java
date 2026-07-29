package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binaryContent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageContentEmptyException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;
  private final ApplicationEventPublisher eventPublisher;

  //create
  @Transactional
  @Override
  public MessageDto create(MessageCreateRequest request) {
    log.debug("메시지 생성 시작 - channelId: {}, authorId: {}", request.channelId(), request.authorId());
    // 유저 검증
    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> {
          log.warn("존재하지 않는 유저 - authorId: {}", request.authorId());
          return new UserNotFoundException(request.authorId());
        });
    // 채널 검증
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> {
          log.warn("존재하지 않는 채널 - channelId: {}", request.channelId());
          return new ChannelNotFoundException(request.channelId());
        });

    Message message = new Message(user, channel, request.content());

    if (request.attachments() != null && !request.attachments().isEmpty()) {
      request.attachments().forEach(attachmentRequest ->
          message.getAttachments().add(saveAttachment(attachmentRequest, user.getId()))
      );
    }
    Message saved = messageRepository.save(message);
    MessageDto dto = messageMapper.toDto(saved);
    log.info("메시지 생성 완료 - messageId: {}", message.getId());

    eventPublisher.publishEvent(new MessageCreatedEvent(
        channel.getId(),
        user.getId(),
        request.content(),
        channel.getName(),
        dto
    ));
    return dto;
  }

  //read
  @Override
  @Transactional(readOnly = true)
  public MessageDto findById(UUID messageId) {
    return messageMapper.toDto(findMessageOrThrow(messageId));
  }

  //readAll
  @Override
  @Transactional(readOnly = true)
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor,
      Pageable pageable) {
    Slice<Message> page = cursor == null
        ? messageRepository.findAllByChannel_IdWithDetails(channelId, pageable)  // cursor 없는 쿼리
        : messageRepository.findALLByChannelIdWithCursor(channelId, cursor, pageable);
    return pageResponseMapper.fromSlice(page.map(messageMapper::toDto));
  }

  //update
  @Transactional
  @Override
  @PreAuthorize("@messageAuthGuard.isAuthor(#messageId, authentication.principal.userDto.id)")
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    log.debug("메시지 업데이트 시작 - messageId: {}", messageId);
    Message message = findMessageOrThrow(messageId);
    if (request.newContent() == null || request.newContent().trim().isEmpty()) {
      log.warn("메시지 내용 없음 - messageId: {}", messageId);
      throw new MessageContentEmptyException();
    }
    message.updateContent(request.newContent());

    //첨부파일 수정
    if (request.attachments() != null) {
      deleteAttachments(message.getAttachments());
      message.getAttachments().clear();

      UUID authorId = message.getAuthor().getId();
      request.attachments().forEach(attachmentRequest ->
          message.getAttachments().add(saveAttachment(attachmentRequest, authorId))
      );
    }
    log.info("메시지 업데이트 완료 - messageId: {}", message.getId());
    return messageMapper.toDto(message);
  }

  //delete
  @Transactional
  @Override
  @PreAuthorize("@messageAuthGuard.isAuthor(#messageId, authentication.principal.userDto.id)")
  public void delete(UUID messageId) {
    log.debug("메시지 삭제 시작 - messageId: {}", messageId);
    Message message = findMessageOrThrow(messageId);

    //binaryContent 삭제
    deleteAttachments(message.getAttachments());
    messageRepository.deleteById(messageId);
    log.info("메시지 삭제 완료 - messageId: {}", messageId);
  }

  private Message findMessageOrThrow(UUID messageId) {
    return messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn("메시지 존재하지 않음 - messageId: {}", messageId);
          return new MessageNotFoundException(messageId);
        });
  }

  private BinaryContent saveAttachment(BinaryContentCreateRequest attachmentRequest, UUID ownerId) {
    log.debug("첨부파일 저장 시작");
    BinaryContent binaryContent = new BinaryContent(
        attachmentRequest.contentType(),
        attachmentRequest.bytes()
    );
    binaryContentRepository.save(binaryContent);
    eventPublisher.publishEvent(
        new BinaryContentCreatedEvent(binaryContent.getId(), attachmentRequest.bytes(), ownerId)
    );
    log.debug("첨부파일 저장 완료 - attachmentId: {}", binaryContent.getId());
    return binaryContent;
  }

  // 삭제
  private void deleteAttachments(List<BinaryContent> attachments) {
    log.debug("첨부파일 삭제 시작");
    attachments.forEach(attachment -> binaryContentStorage.delete(attachment.getId()));
    binaryContentRepository.deleteAll(attachments);
    log.debug("첨부파일 삭제 완료 - 삭제 개수: {}", attachments.size());
  }
}
