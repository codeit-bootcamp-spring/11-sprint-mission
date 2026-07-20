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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
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
      // 토큰 유효성 검사 + Registry에 AccessToken을 가지고 있는 JwtInformation이 존재하는지(Registry 상태 검증) 체크
      // false시 다음 필터로 넘기지 않고 즉시 401 Unauthorized 응답을 반환하도록 수정
      // 다음 필터로 넘기게 될 경우 403 Forbidden 발생
      if (!jwtTokenProvider.validateToken(token) ||
          !jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
//        filterChain.doFilter(request, response);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
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
      // NullPointException 등 예상되지 못한 언체크드 예외 포함으로 warn으로 설정
      log.warn("JWT 인증 처리 중 예외 발생 : {}, 예외 타입 : {}",
          e.getMessage(), e.getClass().getSimpleName());

      // 토큰에 예외 발생 시 인증 제거
      SecurityContextHolder.clearContext();
    }

    // 다음 필터로 진행(인증 처리 완료)
    filterChain.doFilter(request, response);
  }

}
