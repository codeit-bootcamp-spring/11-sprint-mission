package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
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
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest request, List<MultipartFile> attachments) {
    log.debug("메시지 생성 시작 - channelId: {}, authorId: {}", request.channelId(), request.authorId());

    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> {
          log.warn("메시지 생성 실패(존재하지 않는 유저) - authorId: {}", request.authorId());
          return new UserNotFoundException(request.authorId());
        });
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> {
          log.warn("메시지 생성 실패(존재하지 않는 채널) - channelId: {}", request.channelId());
          return new ChannelNotFoundException(request.channelId());
        });

    Message message = new Message(request.content(), author, channel);
    List<BinaryContent> attachmentContents = new ArrayList<>();

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        BinaryContent content = new BinaryContent(
            file.getOriginalFilename(), file.getSize(), file.getContentType());
        attachmentContents.add(content);
      }
      binaryContentRepository.saveAll(attachmentContents);
      message.setAttachments(attachmentContents);
    }

    messageRepository.save(message);

    if (attachments != null && !attachments.isEmpty()) {
      List<UUID> uploadedFileIds = new ArrayList<>();
      try {
        for (int i = 0; i < attachments.size(); i++) {
          MultipartFile file = attachments.get(i);
          BinaryContent content = attachmentContents.get(i);
          binaryContentStorage.put(content.getId(), file.getBytes());
          uploadedFileIds.add(content.getId());
        }
      } catch (Exception e) {
        log.error("메시지 첨부파일 스토리지 저장 중 오류 발생", e);
        for (UUID id : uploadedFileIds) {
          try {
            binaryContentStorage.delete(id);
          } catch (Exception deleteEx) {
            log.error("파일 삭제 실패 - 고아 파일 발생 주의: {}", id, deleteEx);
          }
        }
        throw new RuntimeException("파일 저장 중 오류가 발생하여 메시지 생성이 취소되었습니다.");
      }
    }

    log.info("메시지 생성 완료 - messageId: {}", message.getId());
    return messageMapper.toDto(message);
  }

  @Override
  public MessageDto findById(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("메시지 조회 실패(존재하지 않는 메시지) - messageId: {}", id);
          return new MessageNotFoundException(id);
        });
    return messageMapper.toDto(message);
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, int size) {
    PageRequest pageRequest = PageRequest.of(0, size);
    Slice<Message> slice;

    if (cursor == null) {
      slice = messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageRequest);
    } else {
      slice = messageRepository.findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
          channelId, cursor, pageRequest);
    }

    Slice<MessageDto> dtoSlice = slice.map(messageMapper::toDto);
    Instant nextCursor = null;

    if (slice.hasNext() && !slice.getContent().isEmpty()) {
      List<Message> messages = slice.getContent();
      nextCursor = messages.get(messages.size() - 1).getCreatedAt();
    }

    return pageResponseMapper.fromSlice(dtoSlice, nextCursor);
  }

  @Override
  @Transactional
  public MessageDto update(UUID id, MessageUpdateRequest request) {
    log.debug("메시지 수정 시작 - messageId: {}", id);
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("메시지 수정 실패(존재하지 않는 메시지) - messageId: {}", id);
          return new MessageNotFoundException(id);
        });
    message.update(request.newContent());
    log.info("메시지 수정 완료 - messageId: {}", id);
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("메시지 삭제 시작 - messageId: {}", id);

    if (!messageRepository.existsById(id)) {
      log.warn("메시지 삭제 실패(존재하지 않는 메시지) - messageId: {}", id);
      throw new MessageNotFoundException(id);
    }

    messageRepository.deleteById(id);
    log.info("메시지 삭제 완료 - messageId: {}", id);
  }
}
