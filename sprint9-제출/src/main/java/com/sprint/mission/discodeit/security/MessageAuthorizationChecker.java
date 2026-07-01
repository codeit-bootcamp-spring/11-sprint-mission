package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("messageAuthorizationChecker")
@RequiredArgsConstructor
public class MessageAuthorizationChecker {

  private final MessageRepository messageRepository;

  public boolean isAuthor(UUID messageId, DiscodeitUserDetails principal) {
    if (principal == null) {
      return false;
    }

    UUID currentUserId = principal.getUserDto().id();
    return messageRepository.existsByIdAndAuthor_Id(messageId, currentUserId);
  }
}