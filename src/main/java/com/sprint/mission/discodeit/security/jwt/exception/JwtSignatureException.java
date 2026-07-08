package com.sprint.mission.discodeit.security.jwt.exception;

public class JwtSignatureException extends RuntimeException {

  public JwtSignatureException(String message) {
    super(message);
  }

  public JwtSignatureException(String message, Throwable cause) {
    super(message, cause);
  }
}