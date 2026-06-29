package com.sprint.mission.discodeit.security.service;

import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("messageSecurityService")
@RequiredArgsConstructor
public class MessageSecurityService {

  private final MessageRepository messageRepository;

  public boolean isAuthor(UUID messageId, UUID userId) {
    return messageRepository.findById(messageId)
        .map(message -> message.getAuthor() != null &&
            message.getAuthor().getId().equals(userId))
        .orElse(false);
  }
}
