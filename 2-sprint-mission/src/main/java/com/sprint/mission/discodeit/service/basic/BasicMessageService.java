package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageDto.CreateRequest;
import com.sprint.mission.discodeit.dto.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @Transactional
  public MessageDto.Response create(CreateRequest request,
      List<BinaryContentDto.CreateRequest> fileRequests) {
    log.debug("메시지 생성 시작: channelId={}, authorId={}", request.channelId(), request.authorId());

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> ChannelNotFoundException.withId(request.channelId()));

    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> UserNotFoundException.withId(request.authorId()));

    List<BinaryContent> attachments = (fileRequests != null)
        ? fileRequests.stream().map(BinaryContentDto.CreateRequest::toEntity).toList()
        : new ArrayList<>();

    List<UUID> savedFileIds = new ArrayList<>();

    try {
      if (!attachments.isEmpty()) {
        binaryContentRepository.saveAll(attachments);

        for (int i = 0; i < attachments.size(); i++) {
          BinaryContent entity = attachments.get(i);
          BinaryContentDto.CreateRequest fileReq = fileRequests.get(i);

          binaryContentStorage.put(entity.getId(), fileReq.bytes());

          savedFileIds.add(entity.getId());
        }
      }

      Message message = request.toEntity(channel, author, attachments);
      messageRepository.save(message);

      log.info("메시지 생성 완료: messageId={}, channelId={}", message.getId(), channel.getId());
      return messageMapper.toDto(message);

    } catch (RuntimeException e) {
      if (!savedFileIds.isEmpty()) {
        log.warn("메시지 저장 중 오류 발생으로 저장된 이미지 삭제: savedFileIds={}", savedFileIds);
        for (UUID id : savedFileIds) {
          binaryContentStorage.delete(id);
        }
      }
      throw e;
    }
  }


  @Override
  public PageResponse<MessageDto.Response> findAllByChannelId(UUID channelId, Instant cursor,
      Pageable pageable) {
    log.debug("채널 메시지 목록 페이징 조회 시작: channelId={}", channelId);

    Instant effectiveCursor =
        (cursor != null) ? cursor : Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS);
    Slice<Message> messageSlice = messageRepository.findAllByChannelId(channelId, effectiveCursor,
        pageable);
    Slice<MessageDto.Response> responseSlice = messageSlice.map(messageMapper::toDto);

    Instant nextCursor = null;
    if (messageSlice.hasNext() && !messageSlice.getContent().isEmpty()) {
      List<Message> content = messageSlice.getContent();
      nextCursor = content.get(content.size() - 1).getCreatedAt();
    }
    log.info("채널 메시지 목록 페이징 조회 완료: 조회 건수={}", responseSlice.getContent().size());
    return pageResponseMapper.fromSlice(responseSlice, nextCursor);
  }

  @Override
  @Transactional
  @PreAuthorize("@messageSecurity.isAuthor(#id, principal.userDto.id)")
  public MessageDto.Response update(UUID id, MessageDto.UpdateRequest request) {
    log.debug("메시지 업데이트 시작: messageId={}", id);
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> MessageNotFoundException.withId(id));
    message.update(request.newContent());

    log.info("메시지 업데이트 완료: messageId={}", id);
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  @PreAuthorize("@messageSecurity.isAuthor(#id, principal.userDto.id)")
  public void delete(UUID id) {
    log.debug("메시지 삭제 시작: messageId={}", id);

    if (!messageRepository.existsById(id)) {
      throw MessageNotFoundException.withId(id);
    }

    messageRepository.deleteById(id);
    log.info("메시지 삭제 완료: messageId={}", id);
  }
}