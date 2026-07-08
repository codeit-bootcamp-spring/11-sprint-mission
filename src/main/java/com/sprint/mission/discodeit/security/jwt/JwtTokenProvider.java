package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    private final byte[] secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${discodeit.jwt.secret}") String secret,
            @Value("${discodeit.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${discodeit.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(DiscodeitUserDetails userDetails) {
        return generateToken(
                userDetails.getUserDto().id().toString(),
                accessTokenExpiration,
                "access"
        );
    }

    public String generateRefreshToken(DiscodeitUserDetails userDetails) {
        return generateToken(
                userDetails.getUserDto().id().toString(),
                refreshTokenExpiration,
                "refresh"
        );
    }

    public String reissueAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 유효하지 않습니다.");
        }
        return generateToken(
                getSubject(refreshToken),
                accessTokenExpiration,
                "access"
        );
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verified = signedJWT.verify(new MACVerifier(secret));
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return verified
                    && expirationTime != null
                    && expirationTime.after(new Date());
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public Instant getExpiration(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getExpirationTime()
                    .toInstant();
        } catch (ParseException e) {
            throw new IllegalArgumentException("JWT 만료 시간을 확인할 수 없습니다.", e);
        }
    }

    public String getSubject(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getSubject();
        } catch (ParseException e) {
            throw new IllegalArgumentException("JWT subject를 확인할 수 없습니다.", e);
        }
    }

    private String generateToken(
            String subject,
            long expiration,
            String tokenType
    ) {
        Instant now = Instant.now();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusMillis(expiration)))
                .jwtID(UUID.randomUUID().toString())
                .claim("tokenType", tokenType)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        try {
            signedJWT.sign(new MACSigner(secret));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT 발급에 실패했습니다.", e);
        }
    }
}
