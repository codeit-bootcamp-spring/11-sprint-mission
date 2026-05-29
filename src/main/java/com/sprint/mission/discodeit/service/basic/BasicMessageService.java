package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.AttachmentSaveFailedException;
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
import java.util.List;
import java.util.UUID;
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
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;
  private final BinaryContentStorage binaryContentStorage;

  // Create
  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachments) {

    log.debug("[MESSAGE_CREATE_START] 메시지 생성 시작 - 채널 ID={}, 작성자 ID={}, 첨부파일 수={}",
        dto.channelId(), dto.authorId(), attachments != null ? attachments.size() : 0);

    Channel channel = channelRepository.findById(dto.channelId()).orElseThrow(
        () -> {
          log.warn("[MESSAGE_CREATE_FAILED] 메시지 생성 실패 - 채널이 존재하지 않음 - 채널 ID={}", dto.channelId());
          return new ChannelNotFoundException(dto.channelId());
        }
    );

    User author = userRepository.findById(dto.authorId()).orElseThrow(
        () -> {
          log.warn("[MESSAGE_CREATE_FAILED] 메시지 생성 실패 - 유저가 존재하지 않음 - 유저 ID={}", dto.authorId());
          return new UserNotFoundException(dto.authorId());
        }
    );

    Message message = Message.create(dto.content(), channel, author);

    // 첨부파일 등록(선택)
    if (attachments != null && !attachments.isEmpty()) {

      log.debug("[MESSAGE_CREATE_ATTACHMENTS_START] 첨부파일 등록 시작 - 파일 수={}",
          attachments != null ? attachments.size() : 0);

      attachments.forEach(file -> {
        BinaryContent binaryContent = BinaryContent.of(
            file.getOriginalFilename(), file.getSize(),
            file.getContentType()
        );

        binaryContentRepository.save(binaryContent);
        try {
          binaryContentStorage.put(binaryContent.getId(), file.getBytes());
        } catch (IOException e) {
          log.error("[MESSAGE_CREATE_FAILED] 첨부파일 저장 실패 - 파일명={}, 크기={}",
              file.getOriginalFilename(), file.getSize(), e);
          throw new AttachmentSaveFailedException(file.getOriginalFilename());
        }

        message.addAttachment(binaryContent); // message.getAttachments().add(binaryContent) 캡슐화
      });

      log.debug("[MESSAGE_CREATE_ATTACHMENTS_SUCCESS] 첨부파일 등록 완료 - 파일 수={}",
          attachments != null ? attachments.size() : 0);
    }

    messageRepository.save(message);

    log.info(
        "[MESSAGE_CREATE_SUCCESS] 메시지 생성 완료 - 메시지 ID={}, 메시지 생성 시각={}, 채널 ID={}, 작성자 ID={}, 첨부파일 수={}",
        message.getId(), message.getCreatedAt(), channel.getId(), author.getId(),
        attachments != null ? attachments.size() : 0);

    return messageMapper.toDto(message);
  }

  // Read
  @Override
  @Transactional(readOnly = true)
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor) {
    Pageable pageable = PageRequest.of(0, 50); // 50개씩
    Slice<Message> slice = cursor == null
        ? messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable)
        : messageRepository.findMessages(channelId, cursor, pageable);

    Instant nextCursor = slice.hasNext() && slice.hasContent() ?
        slice.getContent().get(slice.getNumberOfElements() - 1).getCreatedAt() : null;

    return pageResponseMapper.fromSlice(slice.map(messageMapper::toDto), nextCursor);
//    return slice.getContent().stream()
//        .map(messageMapper::toDto).toList();
  }

  // Update
  @Override
  @Transactional
  public MessageDto update(UUID id, MessageUpdateRequest dto) {
    log.debug("[MESSAGE_UPDATE_START] 메시지 수정 시작 - 수정할 메시지 ID={}, 요청할 수정 메시지 내용={}",
        id, dto.newContent());

    Message message = messageRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[MESSAGE_UPDATE_FAILED] 메시지 수정 실패 - 존재하지 않음 - 메시지 ID={}", id);
          return new MessageNotFoundException(id);
        }
    );
    message.updateContent(dto.newContent());
    messageRepository.save(message);

    log.info("[MESSAGE_UPDATE_SUCCESS] 메시지 수정 성공 - 수정한 메시지 ID={}, 수정한 메시지 내용={}", id,
        dto.newContent());

    return messageMapper.toDto(message);
  }

  // Delete
  // 기존 메시지만 삭제
  // 고도화 후 첨부파일 삭제 추가
  @Override
  @Transactional
  public void delete(UUID id) {

    log.debug("[MESSAGE_DELETE_START] 메시지 삭제 시작 - 메시지 ID={}", id);

    Message message = messageRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[MESSAGE_DELETE_FAILED] 메시지 삭제 실패 - 존재하지 않음 - 메시지 ID={}", id);
          return new MessageNotFoundException(id);
        }
    );

    // 특정 메시지에 존재하는 첨부파일 삭제
    if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
      log.debug("[MESSAGE_DELETE_ATTACHMENTS_START] 메시지의 첨부파일 삭제 시작 - 메시지 ID={}, 첨부파일 수={}",
          message.getId(), message.getAttachments() == null ? 0 : message.getAttachments().size());

      binaryContentRepository.deleteAll(message.getAttachments());

      log.debug("[MESSAGE_DELETE_ATTACHMENTS_SUCCESS] 첨부파일 삭제 완료 - 메시지 ID={}, 첨부파일 수={}",
          message.getId(), message.getAttachments() == null ? 0 : message.getAttachments().size());
    }

    // 메시지 삭제
    messageRepository.deleteById(id);

    log.info("[MESSAGE_DELETE_SUCCESS] 메시지 삭제 완료 - 메시지 ID={}", id);
  }
}
