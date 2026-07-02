package com.sprint.mission.discodeit.security.evaluator;

import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagePermissionEvaluator {

  private final MessageRepository messageRepository;

  public boolean isOwner(UUID messageId, Authentication authentication) {

    // 로그인 사용자의 ID를 꺼내기 위해 UserDetails 변수 선언
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

    // 로그인 사용자의 ID를 가져옴
    UUID userId = userDetails.getUserDto().id();

    // 메시지 ID를 조회하여 작성자 ID와 로그인 사용자의 ID가 일치할 경우 권한 적용
    // .map()에서 T는 현재 가지고 있는 타입, U는 변환하고 싶은 타입
    // .map(message -> message.getAuthor...equals(userId))에서 message가 T, .equals(userId)가 U
    // 즉 .map(message -> message...equals(userId)) 내부 타입은 Message -> Boolean
    // Jpa Repository의 기본 메서드는 Optional 타입이고 .map()을 통해 Optional<T>에서 Optional<Boolean>이 됨
    // Optional<Boolean> 타입에 .orElse(T)의 T(원하는 타입)에 false를 써서 boolean으로 만들어줌
    return messageRepository.findById(messageId)
        .map(message -> message.getAuthor().getId().equals(userId))
        .orElse(false);
  }

}
