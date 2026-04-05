package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;

  // Create
  @Override
  public Message create(MessageCreateRequest dto, List<MultipartFile> attachments) {
    channelRepository.findById(dto.channelId());
    userRepository.findById(dto.authorId());
    Message message = Message.create(dto.content(), dto.channelId(), dto.authorId());

    // 첨부파일 등록(선택)
    if (attachments != null && !attachments.isEmpty()) {
      attachments.forEach(file -> {
        try {
          BinaryContent binaryContent = BinaryContent.messageAttachment(
              message.getId(),
              file.getBytes(),
              file.getOriginalFilename(),
              file.getContentType()
          );
          binaryContentRepository.insert(binaryContent);
          message.getAttachmentIds().add(binaryContent.getId());
        } catch (IOException e) {
          throw new RuntimeException("첨부파일 처리 실패 : ", e);
        }
      });
    }

    messageRepository.insert(message);

    return message;
  }

  // Read
  @Override
  public List<Message> findAllByChannelId(UUID channelId) {
    List<Message> message = messageRepository.findAllByChannelId(channelId);

    return message;
  }

  // Update
  @Override
  public Message update(UUID id, MessageUpdateRequest dto) {
    Message message = messageRepository.findById(id);
    message.updateContent(dto.newContent());
    messageRepository.update(message);
    System.out.println();

    return message;
  }

  // Delete
  // 기존 메시지만 삭제
  // 고도화 후 첨부파일 삭제 추가
  @Override
  public void delete(UUID id) {
    Message message = messageRepository.findById(id);
    // 첨부파일 삭제
    message.getAttachmentIds().forEach(binaryContentRepository::delete);

    // 메시지 삭제
    messageRepository.delete(id);
  }
}
