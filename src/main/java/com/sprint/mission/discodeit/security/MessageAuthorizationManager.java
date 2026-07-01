package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("messageAuth")
@RequiredArgsConstructor
public class MessageAuthorizationManager {

  private final JPAMessageRepository messageRepository;

  public boolean isAuthor(UUID messageId, DiscodeitUserDetails principal) {
    return messageRepository.findById(messageId)
        .map(m -> m.getAuthor().getId()
            .equals(principal.getUserDto().id()))
        .orElse(false);
  }
}