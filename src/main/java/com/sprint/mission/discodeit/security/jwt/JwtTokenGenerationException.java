package com.sprint.mission.discodeit.security.jwt;

public class JwtTokenGenerationException extends RuntimeException {

  public JwtTokenGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}
