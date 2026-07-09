package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("messageSecurity")
@RequiredArgsConstructor
public class MessageSecurity {

  private final MessageRepository messageRepository;

  public boolean isAuthor(Authentication authentication, UUID messageId) {
    if (!(authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails)) {
      return false;
    }
    UUID currentUserId = userDetails.getUserDto().id();
    return messageRepository.findById(messageId)
        .map(message -> message.getAuthor() != null
            && message.getAuthor().getId().equals(currentUserId))
        .orElse(false);
  }
}
