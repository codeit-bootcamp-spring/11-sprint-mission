package com.sprint.mission.discodeit.security.jwt.provider;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.jwt.exception.JwtSignatureException;
import com.sprint.mission.discodeit.security.jwt.model.TokenType;
import com.sprint.mission.discodeit.security.properties.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final JwtProperties jwtProperties;

  // Access Token 발급
  public String generateAccessToken(String userId) {

    return generateToken(TokenType.ACCESS, userId,
        getTokenExpiration(jwtProperties.getAccessTokenExpiration()));
  }

  // Refresh Token 발급
  public String generateRefreshToken(String userId) {

    return generateToken(TokenType.REFRESH, userId,
        getTokenExpiration(jwtProperties.getRefreshTokenExpiration()));

  }

  // Refresh Token으로 Access Token 갱신(재 발급)
  public String reIssueAccessToken(String refreshToken) {
    if (refreshToken == null || !validateToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    String userId = getSubject(refreshToken);

    return generateAccessToken(userId);
  }

  // 유효성 검사
  public boolean validateToken(String token) {
    try {
      // Header.Payload.Signature 구조로 분해(각각 객체화)
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(createVerifier())) {
        return false;
      }

      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

      // 만료 시간(expiration)이 지금(new Date())보다 뒤인지 체크
      return expiration.after(new Date());

    } catch (Exception e) {
      return false;
    }
  }

  private String generateToken(
      TokenType tokenType,
      String subject,
      Date expiration
  ) {
    try {
      JWSSigner signer = createSigner();

      // claims set
      JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
      builder.subject(subject);
      builder.issueTime(Calendar.getInstance().getTime());
      builder.expirationTime(expiration);

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());

      signedJWT.sign(signer);

      return signedJWT.serialize();
    } catch (JOSEException e) {
      String tokenPrefix = (tokenType == TokenType.ACCESS) ? "Access" : "Refresh";
      throw new JwtSignatureException(tokenPrefix + "Token 생성에 실패하였습니다.", e);
    }
  }

  public Instant getExpiration(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

      return expiration.toInstant();
    } catch (ParseException e) {
      throw new JwtSignatureException("JWT 형식이 올바르지 않습니다", e);
    }
  }

  // 사용자 식별 값(subject) 추출
  public String getSubject(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT
          .getJWTClaimsSet()
          .getSubject();
    } catch (ParseException e) {
      throw new JwtSignatureException("JWT 형식이 올바르지 않습니다", e);
    }
  }

  // 유효기간 생성(분 단위)
  public Date getTokenExpiration(int expirationMinutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, expirationMinutes);
    return calendar.getTime();
  }

  // Signer
  private JWSSigner createSigner() throws JOSEException {
    byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);

    return new MACSigner(keyBytes);
  }

  // Verifier
  private JWSVerifier createVerifier() throws JOSEException {
    byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);

    return new MACVerifier(keyBytes);
  }
}
