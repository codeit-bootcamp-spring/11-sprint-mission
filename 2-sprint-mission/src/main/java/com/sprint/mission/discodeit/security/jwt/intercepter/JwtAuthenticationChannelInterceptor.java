package com.sprint.mission.discodeit.security.jwt.intercepter;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
      return message;
    }

    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.warn("웹소켓 CONNECT에 인증 헤더 없음");
      throw new MessagingException("인증 정보가 없습니다.");
    }

    String token = authHeader.substring(BEARER_PREFIX.length());

    if (!jwtTokenProvider.validateAccessToken(token)
        || !jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
      log.warn("웹소켓 CONNECT 토큰 검증 실패");
      throw new MessagingException("유효하지 않은 토큰입니다.");
    }

    accessor.setUser(createAuthentication(token));
    log.debug("웹소켓 인증 성공: username={}", jwtTokenProvider.getSubject(token));

    return message;
  }

  private UsernamePasswordAuthenticationToken createAuthentication(String token) {
    UserDto.Response userDto = UserDto.Response.builder()
        .id(jwtTokenProvider.getUserId(token))
        .username(jwtTokenProvider.getSubject(token))
        .role(jwtTokenProvider.getRole(token))
        .online(false)
        .build();

    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(userDto, null);

    return new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
  }
}