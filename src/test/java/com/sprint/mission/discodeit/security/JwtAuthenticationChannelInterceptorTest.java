package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.security.authority.UserRole;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationChannelInterceptorTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    JwtRegistry jwtRegistry;

    @Mock
    DiscodeitUserDetailsService userDetailsService;

    @InjectMocks
    JwtAuthenticationChannelInterceptor interceptor;

    @Test
    void preSend_authenticatesConnectFrame() {
        // given
        UUID userId = UUID.randomUUID();
        String accessToken = "access-token";
        DiscodeitUserDetails userDetails = new DiscodeitUserDetails(
                new UserDto(
                        userId,
                        "evan",
                        "evan@test.com",
                        null,
                        true,
                        UserRole.USER
                ),
                "password"
        );
        Message<byte[]> message = connectMessage("Bearer " + accessToken);

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .willReturn(true);
        given(jwtTokenProvider.getSubject(accessToken))
                .willReturn(userId.toString());
        given(userDetailsService.loadUserById(userId)).willReturn(userDetails);

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(StompHeaderAccessor.wrap(result).getUser())
                .extracting("principal")
                .isEqualTo(userDetails);
    }

    @Test
    void preSend_rejectsConnectFrameWithoutToken() {
        Message<byte[]> message = connectMessage(null);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BadCredentialsException.class);
    }

    private Message<byte[]> connectMessage(String authorizationHeader) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeader != null) {
            accessor.setNativeHeader(
                    HttpHeaders.AUTHORIZATION,
                    authorizationHeader
            );
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );
    }
}
