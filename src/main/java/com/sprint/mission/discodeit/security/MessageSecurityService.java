package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service("messageSecurity")
public class MessageSecurityService {

  private final MessageRepository messageRepository;

  @Transactional(readOnly = true)
  public boolean isAuthor(UUID messageId, UUID userId) {
    return messageRepository.findById(messageId)
        .map(message -> message.getAuthor().getId().equals(userId))
        .orElseThrow(() -> MessageNotFoundException.withId(messageId));
  }
}
