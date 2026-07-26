package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      log.debug("stomp connect authentication trial");
      String authHeader = accessor.getFirstNativeHeader("Authorization");
      if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
        log.warn("stomp connect authentication fail: missing Authorization header");
        throw new AuthenticationCredentialsNotFoundException("Missing Authorization header");
      }

      String token = authHeader.substring(BEARER_PREFIX.length());
      if (!jwtTokenProvider.validateAccessToken(token)
          || !jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
        log.warn("stomp connect authentication fail: invalid or expired access token");
        throw new AuthenticationCredentialsNotFoundException("Invalid or expired access token");
      }

      UUID userId = UUID.fromString(jwtTokenProvider.getSubject(token));
      String username = jwtTokenProvider.getUsername(token);
      Role role = Role.valueOf(jwtTokenProvider.getRole(token));

      UserResponse user = new UserResponse(userId, username, null, null, true, role);
      DiscodeitUserDetails userDetails = new DiscodeitUserDetails(user, "");

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      accessor.setUser(authentication);
      log.info("stomp connect authentication success: userId={}", userId);
    }
    return message;
  }
}