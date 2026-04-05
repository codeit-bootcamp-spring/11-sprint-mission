package com.sprint.mission.discodeit.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:3001")
        .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.stream()
        .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
        .map(c -> (MappingJackson2HttpMessageConverter) c)
        .forEach(c -> {
          List<MediaType> types = new ArrayList<>(c.getSupportedMediaTypes());
          types.add(MediaType.APPLICATION_OCTET_STREAM);
          c.setSupportedMediaTypes(types);
        });
  }
}
