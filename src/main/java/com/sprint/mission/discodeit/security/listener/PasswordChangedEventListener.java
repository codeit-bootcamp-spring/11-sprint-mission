package com.sprint.mission.discodeit.security.listener;

import com.sprint.mission.discodeit.event.PasswordChangeEvent;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class PasswordChangedEventListener {

  private final SessionRegistry sessionRegistry;
  private final RememberMeServices rememberMeServices;

  @EventListener
  public void handlePasswordChanged(PasswordChangeEvent event) {
    sessionRegistry.getAllPrincipals().stream()
        .filter(p -> p instanceof DiscodeitUserDetails)
        .map(p -> (DiscodeitUserDetails) p)
        .filter(p -> p.getUserDto().id().equals(event.getUserId()))
        .forEach(p -> {
          List<SessionInformation> sessions = sessionRegistry.getAllSessions(p, false);
          sessions.forEach(SessionInformation::expireNow);
        });

    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      HttpServletResponse response = attributes.getResponse();
      if (response != null) {
        rememberMeServices.loginFail(request, response);
      }
    }
  }
}
