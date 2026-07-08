package com.sprint.mission.discodeit.security.listener;

import com.sprint.mission.discodeit.event.PasswordChangeEvent;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordChangedEventListener {

  private final JwtRegistry jwtRegistry;

  @EventListener
  public void handlePasswordChanged(PasswordChangeEvent event) {
    jwtRegistry.invalidateJwtInformationByUserId(event.getUserId());
  }
}
