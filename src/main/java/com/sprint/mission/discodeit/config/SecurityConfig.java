package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.DiscodeitAuthenticationFailureHandler;
import com.sprint.mission.discodeit.security.DiscodeitAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final DiscodeitAuthenticationSuccessHandler authenticationSuccessHandler;
  private final DiscodeitAuthenticationFailureHandler authenticationFailureHandler;

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
            .successHandler(authenticationSuccessHandler)
            .failureHandler(authenticationFailureHandler)
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
  }