package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final String secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;
  private final String base64EncodedSecretKey;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

    this.secretKey = secretKey;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;

    this.base64EncodedSecretKey = Base64.getEncoder()
        .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  // 토큰 발급
  public String createAccessToken(Authentication authentication) {
    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));
    return createToken(authentication.getName(), authorities, accessTokenExpiration);
  }

  // 토큰 갱신
  public String createRefreshToken(Authentication authentication) {
    return createToken(authentication.getName(), "", refreshTokenExpiration);
  }

  private String createToken(String subject, String authorities, long expirationTime) {
    try {
      byte[] keyBytes = Base64.getDecoder().decode(base64EncodedSecretKey);
      JWSSigner signer = new MACSigner(keyBytes);

      JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
          .subject(subject)
          .issueTime(new Date())
          .expirationTime(new Date(System.currentTimeMillis() + expirationTime));

      if (authorities != null && !authorities.isEmpty()) {
        builder.claim("auth", authorities);
      }

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
      signedJWT.sign(signer);
      return signedJWT.serialize();

    } catch (JOSEException e) {
      log.error("JWT 생성 중 에러 발생", e);
      throw new RuntimeException("JWT 토큰 생성 실패", e);
    }
  }

  // 유효성 검사
  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      byte[] keyBytes = Base64.getDecoder().decode(base64EncodedSecretKey);
      JWSVerifier verifier = new MACVerifier(keyBytes);

      if (!signedJWT.verify(verifier)) {
        log.warn("유효하지 않은 JWT 서명입니다.");
        return false;
      }

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expirationTime == null || expirationTime.before(new Date())) {
        log.warn("만료된 JWT 토큰입니다.");
        return false;
      }

      return true;
    } catch (ParseException | JOSEException e) {
      log.warn("지원되지 않거나 잘못된 형식의 JWT 토큰입니다.", e);
      return false;
    }
  }
}