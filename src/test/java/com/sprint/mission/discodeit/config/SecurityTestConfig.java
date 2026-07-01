package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.security.CustomAccessDeniedHandler;
import com.sprint.mission.discodeit.security.CustomAuthenticationEntryPoint;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @WebMvcTest 슬라이스에서 SecurityConfig 의존성(핸들러 빈들)을 제공하는 공유 테스트 설정.
 * 각 컨트롤러 테스트에서 @Import(SecurityTestConfig.class)로 임포트한다.
 */
@TestConfiguration
public class SecurityTestConfig {

  @Bean
  public LoginSuccessHandler loginSuccessHandler(ObjectMapper objectMapper) {
    return new LoginSuccessHandler(objectMapper);
  }

  @Bean
  public LoginFailureHandler loginFailureHandler(ObjectMapper objectMapper) {
    return new LoginFailureHandler(objectMapper);
  }

  @Bean
  public CustomAuthenticationEntryPoint customAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return new CustomAuthenticationEntryPoint(objectMapper);
  }

  @Bean
  public CustomAccessDeniedHandler customAccessDeniedHandler(ObjectMapper objectMapper) {
    return new CustomAccessDeniedHandler(objectMapper);
  }
}
