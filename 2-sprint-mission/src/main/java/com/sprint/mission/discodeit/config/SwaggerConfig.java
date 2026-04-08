package com.sprint.mission.discodeit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    Info info = new Info()
        .title("Discodeit API 명세서")
        .version("v1.0.0")
        .description("Discodeit의 Swagger REST API 문서입니다.")
        .contact(new Contact()
            .name("SB_11기 이경신")
            .email("dosly2@naver.com"));

    // 동작 서버
    Server localServer = new Server()
        .url("http://localhost:8080")
        .description("로컬 환경 서버");

    return new OpenAPI()
        .info(info)
        .servers(List.of(localServer));
  }
}