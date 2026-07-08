package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("messagePermissionEvaluator")
@RequiredArgsConstructor
public class MessagePermissionEvaluator {

  private final MessageRepository messageRepository;

  public boolean isAuthor(UUID messageId, DiscodeitUserDetails userDetails) {
    // 인증 정보가 없으면 작성자가 아니라고 판단함
    if (messageId == null || userDetails == null) {
      return false;
    }

    Message message = messageRepository.findById(messageId)
        .orElse(null);

    // 메시지가 없거나 작성자가 없으면 권한 없음으로 판단함
    if (message == null || message.getAuthor() == null) {
      return false;
    }

    // 메시지 작성자 id와 현재 로그인 사용자 id가 같은지 확인함
    return message.getAuthor().getId().equals(userDetails.getId());
  }
}