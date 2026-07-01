package com.sprint.mission.discodeit.security.listener;

import com.sprint.mission.discodeit.event.PasswordChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordChangedEventListener {

  @EventListener
  public void handlePasswordChanged(PasswordChangeEvent event) {
    log.info("비밀번호 변경 감지 - userId: {} (기존 세션/토큰 무효화는 JwtRegistry 구현 후 반영 예정)",
        event.getUserId());
  }
}
