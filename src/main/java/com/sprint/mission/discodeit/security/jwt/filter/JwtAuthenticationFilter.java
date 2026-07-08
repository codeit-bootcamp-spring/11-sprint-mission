package com.sprint.mission.discodeit.security.jwt.filter;

import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  private final DiscodeitUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    // Authorization Header를 가져옴
    String header = request.getHeader("Authorization");

    // Authorization 헤더가 없거나 Bearer Authorization이 아닐 경우 인증 처리 없이 통과
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // "Bearer " 다음 값(7글자), 즉 토큰값만을 가져옴
    String token = header.substring(7);

    try {
      // 토큰 유효성 검사(false시 인증 처리 없이 통과)
      if (!jwtTokenProvider.validateToken(token)) {
        filterChain.doFilter(request, response);
        return;
      }

      // Registry 상태 검증
      if (!jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
        filterChain.doFilter(request, response);
        return;
      }

      // 토큰에서 사용자 식별 정보(subject)를 추출 후 UUID로 변환
      UUID userId = UUID.fromString(jwtTokenProvider.getSubject(token));

      // UserDetails 조회
      DiscodeitUserDetails userDetails =
          (DiscodeitUserDetails) userDetailsService.loadUserById(userId);

      // Authentication 객체 생성
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());

      // SecurityContext 저장(인증 완료 처리)
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception e) {
      // 토큰에 예외 발생 시 인증 제거
      SecurityContextHolder.clearContext();
    }

    // 다음 필터로 진행(인증 처리 완료)
    filterChain.doFilter(request, response);
  }

}
