package com.sprint.mission.discodeit.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.exception.auth.AccessTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailsService userDetailsService;
  private final JwtRegistry jwtRegistry;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    // 토큰이 없으면 비인증 요청으로 다음 필터에 위임한다. (인증이 필요한 경우 EntryPoint가 401 처리)
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(BEARER_PREFIX.length());

    // 토큰이 있지만 유효하지 않으면 즉시 401을 반환한다.
    if (!jwtTokenProvider.validateToken(token)
        || !jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
      sendUnauthorized(response);
      return;
    }

    String username = jwtTokenProvider.getUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }

  private void sendUnauthorized(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ErrorResponse errorResponse = new ErrorResponse(new AccessTokenInvalidException(),
        HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}