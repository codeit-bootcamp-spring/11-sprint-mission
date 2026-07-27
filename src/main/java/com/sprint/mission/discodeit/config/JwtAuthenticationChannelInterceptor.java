package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final DiscodeitUserDetailsService userDetailsService;

  // Message : 클라이언트가 보낸 STOMP 요청
  // MessageChannel : 메시지가 지나가는 통로(채널)
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    // Message 구조 : Message - Headers[Command, Authorization(커스텀이라 getFirstNativeHeader()로 가져와야함) 등..], Payload
    // StompHeaderAccessor는 Message의 Headers에 존재하는 헤더들을 쉽게 가져올 수 있도록 도와줌
    // 예시 accessor.getCommand() : STOMP 요청 종류를 가져옴(CONNECT, SEND, SUBCRIBE)
    // StompHeaderAccessor implements SimpMessageHeaderAccessor implements NativeMessageHeaderAccessor implements MessageHeaderAccessor
    // StompHeaderAccessor가 자식, MessageHeaderAccessor가 부모
    // .getAccessor()가 T 타입 반환, 즉 StompHeaderAccessor 타입 반환
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    // STOMP 요청이 CONNECT일때만 인증
    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

      // 현재 프론트에 STOMP 요청 시 Authorization 커스텀 헤더도 같이 넣도록 포함되어있음
      // 그렇기 때문에 커스텀 헤더를 가져오는 .getFirstNativeHeader를 통해 Authorization 헤더를 가져와야함
      String authorization = accessor.getFirstNativeHeader("Authorization");

      // 인증 헤더가 없거나 Bearer 토큰이 아닐 경우
      if (authorization == null || !authorization.startsWith("Bearer ")) {
        throw new AuthenticationCredentialsNotFoundException("Authorization 헤더가 없습니다.");
      }

      // "Bearer "
      String token = authorization.substring(7);

      try {

        // "Bearer " 토큰 유효성, 현재 활성 상태인지 검사
        if (!jwtTokenProvider.validateToken(token)
            || !jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
          throw new BadCredentialsException("유효하지 않은 토큰입니다.");
        }

        // jwt의 "sub"는 userId.toString()
        UUID userId = UUID.fromString(jwtTokenProvider.getSubject(token));

        // UserDetails 조회
        DiscodeitUserDetails userDetails =
            (DiscodeitUserDetails) userDetailsService.loadUserById(userId);

        // 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());

        // WebSocket에 인증된 사용자를 설정(WebSocket 세션에 사용자 정보 저장)
        accessor.setUser(authentication);

      } catch (Exception e) {
        log.warn("WebSocket JWT 인증 실패 : {}, 예외 타입 : {}", e.getMessage(),
            e.getClass().getSimpleName());
      }
    }

    // setUser 까지 성공하여 try 구문 끝나면 message 헤더에는 User 헤더가 추가됨
    return message;
  }

}
