package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.dto.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.dto.MessageCreatedEvent;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest dto,
      List<BinaryContentCreateRequest> binaryContentDto) {
    log.debug("메시지 생성 시작 - 입력값: {}, {}", dto, binaryContentDto);

    User author = userRepository.findById(dto.authorId())
        .orElseThrow(
            () -> new UserNotFoundException(dto.authorId()));
    Channel channel = channelRepository.findById(dto.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(dto.channelId()));

    List<BinaryContent> attachments = binaryContentDto.stream()
        .map(request -> new BinaryContent(request.fileName(),
            (long) request.bytes().length, request.contentType()))
        .toList();

    attachments = binaryContentRepository.saveAll(attachments);

    for (int i = 0; i < attachments.size(); i++) {
      eventPublisher.publishEvent(new BinaryContentCreatedEvent(attachments.get(i).getId(),
          binaryContentDto.get(i).bytes(), author.getId()));
    }

    Message message = new Message(author, channel, dto.content(), attachments);
    messageRepository.save(message);

    MessageDto messageDto = messageMapper.toDto(message);

    eventPublisher.publishEvent(
        new MessageCreatedEvent(messageDto, Instant.now(), channel.getName()));
    log.info("메시지 생성 완료 - messageId: {}, 첨부파일 수: {}", message.getId(), attachments.size());

    return messageDto;
  }

  // 채팅방에 들어가서 위로 스크롤하면 예전 메시지가 계속 뜨는 로직
  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor) {
    if (!channelRepository.existsById(channelId)) {
      throw new ChannelNotFoundException(channelId);
    }

    Pageable pageable = PageRequest.of(0, 50);

    Slice<Message> slice;

    if (cursor == null) {
      // 처음엔 무조건 맨 처음부터 최신순으로 50개
      slice = messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId,
          pageable);
    } else {
      // 커서(시간)보다 옛날 메시지를 최신순으로 50개
      slice = messageRepository.findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(channelId,
          cursor, pageable);
    }

    Slice<MessageDto> dtoSlice = slice.map(messageMapper::toDto);

    return pageResponseMapper.fromSlice(dtoSlice, MessageDto::createdAt);
  }

  @Override
  @Transactional
  @PreAuthorize("@messageSecurity.isAuthor(#id, principal.userDto.id)")
  public MessageDto update(UUID id, MessageUpdateRequest dto) {
    log.debug("메시지 수정 시작 - 입력값: {}", dto);

    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new MessageNotFoundException(id));

    message.update(dto.newContent());
    log.info("메시지 수정 완료 - messageId: {}", message.getId());

    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  @PreAuthorize("@messageSecurity.isAuthor(#id, principal.userDto.id)")
  public void delete(UUID id) {
    log.debug("메시지 삭제 시작 - 입력값: {}", id);

    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new MessageNotFoundException(id));

    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
      binaryContentRepository.deleteAll(message.getAttachments());
    }

    messageRepository.delete(message);
    log.info("메시지 삭제 완료 - messageId: {}", message.getId());
  }
}