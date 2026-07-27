package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.User.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final JwtAuthenticationChannelInterceptor jwtAuthenticationChannelInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {

    // 클라이언트가 서버의 메시지를 구독하는 경로 접두사
    registry.enableSimpleBroker("/sub");

    // 클라이언트에서 메시지를 서버로 발행하는 경로 접두사
    registry.setApplicationDestinationPrefixes("/pub");

  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {

    // "/ws"에서 WebSocket, SockJS 클라이언트 연결을 지원
    // 분산환경 구성 시 CORS 설정
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("http://localhost:3000")
        .withSockJS();

  }

  // JWT 인증을 WebSocket 인증 단계에도 추가(이 과정을 거치지 않으면 401 에러가 나오면서 정상 동작이 되지 않음)
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {

    // JwtAuthentication Channel Interceptor 에서 인증 후 SecurityContext Channel Interceptor로 넘어감
    // SecurityContext Channel Interceptor에서 Authorization Channel Interceptor로 넘어감
    // 인증 -> security context 등록 -> 인가
    registration.interceptors(
        jwtAuthenticationChannelInterceptor,
        new SecurityContextChannelInterceptor(),
        authorizationChannelInterceptor()
    );
  }

  private AuthorizationChannelInterceptor authorizationChannelInterceptor() {
    return new AuthorizationChannelInterceptor(
        // 모든 STOMP 메시지(CONNECT, SEND, SUBSCRIBE 등)를 요청했을 때 그 요청을 보낸 사용자의 권한이 USER인지 확인 후 통과됨
        MessageMatcherDelegatingAuthorizationManager.builder()
            .anyMessage()
            .hasRole(Role.USER.name())
            .build()
    );
  }
}
