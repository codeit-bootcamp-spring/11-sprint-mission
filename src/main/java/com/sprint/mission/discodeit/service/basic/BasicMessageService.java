package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.common.PageResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.notification.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.websocket.MessagePublishedEvent;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageWithoutChannelAccessException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageMapper mapper;
  private final PageMapper pageMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Override
  public MessageResponse createMessage(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    log.debug("message create trial: request={}, attachments-count={}", messageCreateRequest,
        binaryContentCreateRequests.size());
    User author = this.userRepository.findById(messageCreateRequest.authorId())
        .orElseThrow(() -> UserNotFoundException.withId(messageCreateRequest.authorId()));
    Channel channel = this.channelRepository.findById(messageCreateRequest.channelId())
        .orElseThrow(() -> ChannelNotFoundException.withId(messageCreateRequest.channelId()));

    if (channel.isPrivate() && !this.readStatusRepository.existsByUserAndChannel(author, channel)) {
      throw MessageWithoutChannelAccessException.withUserAndChannel(author.getId(),
          channel.getId());
    }

    Set<UUID> attachmentReceiverIds = resolveAttachmentReceiverIds(channel);
    List<BinaryContent> attachments = new ArrayList<>();
    if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
      attachments = binaryContentCreateRequests.stream()
          .map(req -> {
            BinaryContent attachment = new BinaryContent(req.fileName(), req.size(),
                req.contentType());
            this.eventPublisher.publishEvent(
                new BinaryContentCreatedEvent(attachment.getId(), req.bytes(),
                    attachmentReceiverIds));
            return attachment;
          })
          .toList();
    }

    Message message = new Message(
        messageCreateRequest.content(),
        channel,
        author,
        attachments
    );
    this.messageRepository.save(message);

    this.eventPublisher.publishEvent(new MessageCreatedEvent(
        channel.getId(), channel.getName(), author.getId(), author.getUsername(),
        message.getContent()));

    MessageResponse response = this.mapper.toResponse(message);
    this.eventPublisher.publishEvent(new MessagePublishedEvent(channel.getId(), response));

    log.info("message create success: id={}, channelId={}, authorId={}, attachments-count={}",
        message.getId(), channel.getId(), author.getId(), attachments.size());
    return response;
  }

  @Override
  public PageResponse<MessageResponse> findAllByChannelId(UUID channelId, Instant cursor,
      Pageable pageable) {
    log.debug("message find-all-by-channel-id trial: channelId={}, cursor={}, pageable={}",
        channelId, cursor, pageable);
    if (!this.channelRepository.existsById(channelId)) {
      throw ChannelNotFoundException.withId(channelId);
    }

    Slice<MessageResponse> slice = (cursor == null
        ? this.messageRepository.findAllByChannelIdOrderByCreatedAtDesc(
        channelId, pageable)
        : this.messageRepository.findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(channelId,
            cursor, pageable))
        .map(this.mapper::toResponse);
    PageResponse<MessageResponse> result = this.pageMapper.fromSlice(slice,
        MessageResponse::createdAt);
    log.info("message find-all-by-channel-id success: channelId={}, size={}, hasNext={}",
        channelId, result.content().size(), result.hasNext());
    return result;
  }

  @PostAuthorize("hasRole('ADMIN') or returnObject.author.id == authentication.principal.user.id")
  @Transactional
  @Override
  public MessageResponse updateMessage(UUID id, MessageUpdateRequest messageUpdateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    log.debug("message update trial: id={}, request={}, attachments-count={}", id,
        messageUpdateRequest, binaryContentCreateRequests.size());
    Message message = this.messageRepository.findById(id)
        .orElseThrow(() -> MessageNotFoundException.withId(id));

    if (messageUpdateRequest != null) {
      if (messageUpdateRequest.newContent() != null && !messageUpdateRequest.newContent()
          .isBlank()) {
        message.updateContent(messageUpdateRequest.newContent());
      }
    }

    Set<UUID> attachmentReceiverIds = resolveAttachmentReceiverIds(message.getChannel());
    List<BinaryContent> newAttachments = new ArrayList<>();
    if (binaryContentCreateRequests != null && !binaryContentCreateRequests.isEmpty()) {
      newAttachments = binaryContentCreateRequests.stream()
          .map(req -> {
            BinaryContent attachment = new BinaryContent(req.fileName(), req.size(),
                req.contentType());
            this.eventPublisher.publishEvent(
                new BinaryContentCreatedEvent(attachment.getId(), req.bytes(),
                    attachmentReceiverIds));
            return attachment;
          })
          .toList();
    }
    if (!newAttachments.isEmpty()) {
      message.replaceAttachments(newAttachments);
    }

    log.info("message update success: id={}, attachments-count={}", id, newAttachments.size());
    return this.mapper.toResponse(message);
  }

  @PreAuthorize("hasRole('ADMIN') or @messageSecurity.isAuthor(#id, authentication.principal.user.id)")
  @Transactional
  @Override
  public void deleteMessage(UUID id) {
    log.debug("message delete trial: id={}", id);
    Message message = this.messageRepository.findById(id)
        .orElseThrow(() -> MessageNotFoundException.withId(id));

    this.messageRepository.delete(message);

    log.info("message delete success: id={}", id);
  }

  private Set<UUID> resolveAttachmentReceiverIds(Channel channel) {
    if (!channel.isPrivate()) {
      return null;
    }
    return this.readStatusRepository.findAllByChannelId(channel.getId()).stream()
        .map(readStatus -> readStatus.getUser().getId())
        .collect(Collectors.toSet());
  }
}