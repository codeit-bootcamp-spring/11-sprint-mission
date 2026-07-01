package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.Http401AuthenticationEntryPoint;
import com.sprint.mission.discodeit.security.handler.Http403AccessDeniedHandler;
import com.sprint.mission.discodeit.security.handler.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.security.filter.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.jwt.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
  private final LoginFailureHandler loginFailureHandler;
  private final DiscodeitUserDetailsService userDetailsService;
  private final Http401AuthenticationEntryPoint authenticationEntryPoint;
  private final Http403AccessDeniedHandler accessDeniedHandler;
  private final JwtLogoutHandler jwtLogoutHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy(
        "ROLE_ADMIN > ROLE_CHANNEL_MANAGER\n" +
            "ROLE_CHANNEL_MANAGER > ROLE_USER"
    );
  }

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
      JwtRegistry jwtRegistry) {
    return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, jwtRegistry);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter)
      throws Exception {
    http.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()));

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(
            "/",
            "/index.html",
            "/assets/**",
            "/favicon.ico"
        ).permitAll()
        .requestMatchers(
            "/api/auth/refresh",
            "/api/auth/**",
            "/api/users/",
            "api/users/**"
        ).permitAll()
        .anyRequest().authenticated()
    );

    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(authenticationEntryPoint)
        .accessDeniedHandler(accessDeniedHandler)
    );

    http.sessionManagement(management -> management
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    http.formLogin(login -> login
        .loginProcessingUrl("/api/auth/login")
        .successHandler(jwtLoginSuccessHandler)
        .failureHandler(loginFailureHandler)
    );

    http.logout(logout -> logout
        .logoutUrl("/api/auth/logout")
        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
        .addLogoutHandler(jwtLogoutHandler)
    );

    http.userDetailsService(userDetailsService);

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public JwtRegistry jwtRegistry() {
    return new InMemoryJwtRegistry(1);
  }
}
