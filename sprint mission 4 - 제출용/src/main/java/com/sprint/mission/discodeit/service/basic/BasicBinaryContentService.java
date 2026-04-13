package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @Transactional
  public BinaryContent create(BinaryContentCreateRequest request) {
    if (request.getUserId() == null && request.getMessageId() == null) {
      throw DiscodeitInvalidInputException.blankField("userId or messageId");
    }

    if (request.getUserId() != null && request.getMessageId() != null) {
      throw new DiscodeitInvalidInputException("userId와 messageId는 동시에 가질 수 없습니다.");
    }

    if (request.getContent() == null || request.getContent().length == 0) {
      throw DiscodeitInvalidInputException.blankField("content");
    }

    byte[] bytes = request.getContent();
    Long size = (long) bytes.length;

    BinaryContent binaryContent = new BinaryContent(
        request.getFileName(),
        size,
        request.getContentType()
    );

    binaryContent.validateService();
    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

    binaryContentStorage.put(savedBinaryContent.getId(), bytes);

    if (request.getUserId() != null) {
      User user = userRepository.findById(request.getUserId())
          .orElseThrow(() -> DiscodeitNotFoundException.user(request.getUserId()));

      user.setProfile(savedBinaryContent);
    } else {
      Message message = messageRepository.findById(request.getMessageId())
          .orElseThrow(() -> DiscodeitNotFoundException.message(request.getMessageId()));

      List<BinaryContent> attachments = message.getAttachments();
      if (attachments == null) {
        attachments = new ArrayList<>();
        message.setAttachments(attachments);
      }
      attachments.add(savedBinaryContent);
    }

    return savedBinaryContent;
  }

  @Override
  @Transactional(readOnly = true)
  public BinaryContent find(UUID id) {
    return binaryContentRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.binaryContent(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.binaryContent(id));

    binaryContentRepository.delete(binaryContent);
  }
}
