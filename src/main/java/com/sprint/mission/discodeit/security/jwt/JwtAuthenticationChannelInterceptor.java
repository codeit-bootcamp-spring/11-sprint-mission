package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.StompAuthenticationException;
import com.sprint.mission.discodeit.exception.auth.JwtExpiredException;
import com.sprint.mission.discodeit.exception.auth.JwtSignatureException;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetailService;
import java.util.UUID;
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

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer";

  private final JwtTokenProvider tokenProvider;
  private final DiscodeitUserDetailService userDetailService;
  private final JwtRegistry jwtRegistry;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);
    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String authorization = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

      if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
        log.debug("STOMP CONNECT - Authorization 헤더 없음");
        throw new StompAuthenticationException(ErrorCode.AUTHENTICATION_FAILED);
      }

      String token = authorization.substring(BEARER_PREFIX.length());

      try {
        tokenProvider.validateTokenOrThrow(token);

        if (!jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
          throw new StompAuthenticationException(ErrorCode.AUTHENTICATION_FAILED);
        }

        String subject = tokenProvider.getSubject(token);
        UserDetails userDetails = userDetailService.loadUserByUserId(UUID.fromString(subject));

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        // STOMP에 인증정보 저장
        accessor.setUser(authentication);
        log.debug("STOMP CONNECT 인증 성공 - userId: {}", subject);
      } catch (JwtExpiredException e) {
        throw new StompAuthenticationException(ErrorCode.JWT_EXPIRED, e);
      } catch (JwtSignatureException e) {
        throw new StompAuthenticationException(ErrorCode.JWT_SIGNATURE_INVALID, e);
      }
    }
    return message;
  }

}
