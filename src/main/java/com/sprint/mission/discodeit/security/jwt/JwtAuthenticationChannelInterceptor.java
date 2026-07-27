package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailsService userDetailsService;
  private final JwtRegistry jwtRegistry;
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String authHeader = accessor.getFirstNativeHeader("Authorization");

      if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
        String token = authHeader.substring(BEARER_PREFIX.length());

        if (jwtTokenProvider.validateToken(token)
            && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
          String subject = jwtTokenProvider.getSubject(token);
          UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userDetails, null,
                  userDetails.getAuthorities());

          accessor.setUser(authentication);
          log.debug("STOMP 웹소켓 CONNECT 인증 성공 - username: {}", subject);
        } else {
          log.warn("STOMP 웹소켓 CONNECT 인증 실패 - 유효하지 않은 토큰");
          throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
      } else {
        log.warn("STOMP 웹소켓 CONNECT 인증 실패 - Authorization 헤더 누락 또는 형식 오류");
        throw new IllegalArgumentException("인증 정보가 누락되었습니다.");
      }
    }
    return message;
  }
}
