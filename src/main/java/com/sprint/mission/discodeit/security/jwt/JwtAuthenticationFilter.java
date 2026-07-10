package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.security.service.DiscodeitUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final DiscodeitUserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = resolveToken(request);

    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!isValidAccessToken(token)) {
      sendUnauthorized(response);
      return;
    }

    UUID userId = jwtTokenProvider.getUserId(token);
    String username = jwtTokenProvider.getUsername(token);

    UserDetails userDetails;
    try {
      userDetails = userDetailsService.loadUserByUsername(username);
    } catch (UsernameNotFoundException e) {
      sendUnauthorized(response);
      return;
    }

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }

  private boolean isValidAccessToken(String token) {
    return jwtTokenProvider.validateToken(token)
        && jwtTokenProvider.isAccessToken(token)
        && jwtRegistry.hasActiveJwtInformationByAccessToken(token);
  }

  private void sendUnauthorized(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
