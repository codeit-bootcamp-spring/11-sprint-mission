package com.sprint.mission.discodeit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final JwtRegistry jwtRegistry;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

    // Authorization 헤더가 없거나 Bearer 토큰 형식이 아니면 JWT 인증을 시도하지 않음
    if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());

    // access token 자체가 유효하지 않으면 인증하지 않음
    if (!jwtTokenProvider.validateToken(accessToken)) {
      filterChain.doFilter(request, response);
      return;
    }

    // registry에 등록된 access token이 아니면 인증하지 않음
    if (!jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken)) {
      filterChain.doFilter(request, response);
      return;
    }

    String username = jwtTokenProvider.getUsername(accessToken);

    // 이미 인증 정보가 있으면 중복 인증하지 않음
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );

      // 요청 상세 정보를 인증 객체에 저장함
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      // SecurityContext에 인증 객체를 저장해서 이후 컨트롤러/인가에서 사용할 수 있게 함
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}