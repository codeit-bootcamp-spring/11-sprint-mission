package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final byte[] accessSecretKey;
  private final byte[] refreshSecretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtTokenProvider(
      @Value("${discodeit.jwt.access-token-secret}") String accessSecret,
      @Value("${discodeit.jwt.refresh-token-secret}") String refreshSecret,
      @Value("${discodeit.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${discodeit.jwt.refresh-token-expiration}") long refreshTokenExpiration
  ) {
    this.accessSecretKey = accessSecret.getBytes(StandardCharsets.UTF_8);
    this.refreshSecretKey = refreshSecret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    return generateToken(
        userDetails.getUser().id().toString(),
        accessSecretKey,
        accessTokenExpiration,
        Map.of(
            "username", userDetails.getUsername(),
            "role", userDetails.getUser().role().name()
        )
    );
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    return generateToken(
        userDetails.getUser().id().toString(),
        refreshSecretKey,
        refreshTokenExpiration,
        Map.of()
    );
  }

  private String generateToken(String subject, byte[] secret, long expirationMs,
      Map<String, Object> customClaims) {
    Instant now = Instant.now();
    try {
      JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
          .subject(subject)
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plusMillis(expirationMs)));
      customClaims.forEach(builder::claim);
      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
      signedJWT.sign(new MACSigner(secret));
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("failed to generate jwt token", e);
    }
  }

  public boolean validateAccessToken(String token) {
    return verify(token, accessSecretKey);
  }

  public boolean validateRefreshToken(String token) {
    return verify(token, refreshSecretKey);
  }

  private boolean verify(String token, byte[] secret) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      if (!signedJWT.verify(new MACVerifier(secret))) {
        log.debug("jwt signature invalid");
        return false;
      }
      if (!signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().isAfter(Instant.now())) {
        log.debug("jwt expired");
        return false;
      }
      return true;
    } catch (ParseException e) {
      log.debug("jwt parse failed: {}", e.getMessage());
      return false;
    } catch (JOSEException e) {
      log.debug("jwt signature verification failed: {}", e.getMessage());
      return false;
    }
  }

  public Instant getExpiration(String token) {
    return parseToken(token).getExpirationTime().toInstant();
  }

  public String getSubject(String token) {
    return parseToken(token).getSubject();
  }

  public String getUsername(String token) {
    return Optional.ofNullable((String) parseToken(token).getClaim("username"))
        .orElseThrow(() -> new IllegalStateException("username claim not found in token"));
  }

  public String getRole(String token) {
    return Optional.ofNullable((String) parseToken(token).getClaim("role"))
        .orElseThrow(() -> new IllegalStateException("role claim not found in token"));
  }

  private JWTClaimsSet parseToken(String token) {
    try {
      return SignedJWT.parse(token).getJWTClaimsSet();
    } catch (ParseException e) {
      throw new IllegalStateException("failed to parse jwt token", e);
    }
  }
}