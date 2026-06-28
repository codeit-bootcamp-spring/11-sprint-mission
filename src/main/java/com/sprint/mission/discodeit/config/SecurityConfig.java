package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.DiscodeitAccessDeniedHandler;
import com.sprint.mission.discodeit.security.DiscodeitAuthenticationEntryPoint;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import com.sprint.mission.discodeit.security.SpaCsrfTokenRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      LoginSuccessHandler loginSuccessHandler,
      LoginFailureHandler loginFailureHandler,
      DiscodeitAuthenticationEntryPoint authenticationEntryPoint,
      DiscodeitAccessDeniedHandler accessDeniedHandler) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/login")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler)
        )
        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/csrf-token"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/users"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/login"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/logout"),
                new NegatedRequestMatcher(AntPathRequestMatcher.antMatcher("/api/**"))
            ).permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )
    ;
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
