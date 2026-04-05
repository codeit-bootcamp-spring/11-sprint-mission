package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public MessageDto create(MessageCreateRequest request, List<MultipartFile> attachments) {
    if (userRepository.findById(request.authorId()).isEmpty()) {
      throw new IllegalArgumentException("유저가 존재하지 않습니다.");
    }
    if (channelRepository.findById(request.channelId()) == null) {
      throw new IllegalArgumentException("채널이 존재하지 않습니다.");
    }
    Message message = new Message(request.content(), request.authorId(), request.channelId());
    if (attachments != null && attachments.isEmpty()) {
      List<UUID> attachmentIds = new ArrayList<>();
      for (MultipartFile file : attachments) {
        try {
          BinaryContent content = new BinaryContent(file.getBytes(),
              file.getOriginalFilename(), file.getSize(), file.getContentType());
          binaryContentRepository.save(content);
          attachmentIds.add(content.getId());
        } catch (IOException e) {
          throw new RuntimeException("파일 처리 중 오류 발생", e);
        }
      }
      message.setAttachmentIds(attachmentIds);
    }
    messageRepository.save(message);
    return toDto(message);
  }

  @Override
  public MessageDto findById(UUID id) {
    Message message = messageRepository.findById(id);
    if (message == null) {
      throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }
    return toDto(message);
  }

  @Override
  public List<MessageDto> findAllByChannelId(UUID channelId) {
    return messageRepository.findByChannelId(channelId).stream().
        map(this::toDto).collect(Collectors.toList());
  }

  @Override
  public void update(UUID id, MessageUpdateRequest request) {
    Message message = messageRepository.findById(id);
    if (message == null) {
      throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }
    message.update(request.newContent());
    messageRepository.save(message);
  }

  @Override
  public void delete(UUID id) {
    Message message = messageRepository.findById(id);
    if (message == null) {
      throw new IllegalArgumentException("메시지를 찾을 수 없습니다.");
    }
    if (message.getAttachmentIds() != null) {
      for (UUID attachmentId : message.getAttachmentIds()) {
        binaryContentRepository.delete(attachmentId);
      }
    }
    messageRepository.delete(id);
  }

  private MessageDto toDto(Message message) {
    return new MessageDto(message.getId(), message.getAuthorId(),
        message.getChannelId(), message.getContent(), message.getAttachmentIds(),
        message.getCreatedAt(), message.getUpdatedAt());
  }
}
