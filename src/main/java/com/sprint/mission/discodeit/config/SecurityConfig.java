package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.DiscodeitAuthenticationFailureHandler;
import com.sprint.mission.discodeit.security.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtLogoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
  private final DiscodeitAuthenticationFailureHandler authenticationFailureHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtLogoutHandler jwtLogoutHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    SecurityFilterChain securityFilterChain = http
        .csrf(csrf -> csrf
            // SPA 환경에서 사용할 CSRF 설정함
            // XSRF-TOKEN 쿠키를 발급하고 클라이언트가 X-XSRF-TOKEN 헤더로 다시 전송함
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        .authorizeHttpRequests(auth -> auth
            // CSRF 토큰 발급 API는 로그인 전에도 호출되어야 하므로 허용함
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()

            // 회원가입 API는 로그인 전에도 호출되어야 하므로 허용함
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

            // 로그인/로그아웃 요청은 Spring Security 필터가 처리해야 하므로 허용함
            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

            // 정적 리소스 접근 허용함
            .requestMatchers(
                "/",
                "/index.html",
                "/favicon.ico",
                "/assets/**",
                "/*.js",
                "/*.css",
                "/user-list.html"
            ).permitAll()

            // Swagger/OpenAPI 문서 접근 허용함
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-ui.html"
            ).permitAll()

            // Actuator 접근 허용함
            .requestMatchers("/actuator/**").permitAll()

            // 위에서 허용하지 않은 모든 요청은 인증 필요함
            .anyRequest().authenticated()
        )
        .formLogin(formLogin -> formLogin
            // Spring Security가 처리할 로그인 URL 지정함
            .loginProcessingUrl("/api/auth/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .successHandler(jwtLoginSuccessHandler)
            .failureHandler(authenticationFailureHandler)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .logout(logout -> logout
            // Spring Security가 처리할 로그아웃 URL 지정함
            .logoutUrl("/api/auth/logout")

            // refresh token 쿠키를 기준으로 registry의 JWT 정보를 무효화함
            .addLogoutHandler(jwtLogoutHandler)

            // 로그아웃 성공 시 응답 본문 없이 204 반환함
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))

            .invalidateHttpSession(false)

            // 브라우저에 남아있는 세션 쿠키 삭제 요청함
            .deleteCookies(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME)
        )
        .sessionManagement(session -> session
            // JWT 기반 인증에서는 서버 세션을 생성하지 않음
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .build();

    // 등록된 Spring Security 필터 목록 확인용 로그임
    securityFilterChain.getFilters()
        .forEach(
            filter -> log.debug("Security filter registered: {}", filter.getClass().getName()));

    return securityFilterChain;
  }

    @Bean
    public PasswordEncoder passwordEncoder () {
      // BCrypt 기반 비밀번호 해시 인코더 등록함
      // 회원가입, 비밀번호 변경, 로그인 검증에서 같은 방식으로 사용함
      return new BCryptPasswordEncoder();
    }


    @Bean
    public RoleHierarchy roleHierarchy() {
      // ADMIN은 CHANNEL_MANAGER 권한을 포함함
      // CHANNEL_MANAGER는 USER 권한을 포함함
      return RoleHierarchyImpl.fromHierarchy("""
       ROLE_ADMIN > ROLE_CHANNEL_MANAGER
       ROLE_CHANNEL_MANAGER > ROLE_USER
       """);
    }
  }