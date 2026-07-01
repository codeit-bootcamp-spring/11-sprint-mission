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
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    private final DiscodeitUserDetailsService userDetailsService;


    public String issueAccessToken(UserDetails userDetails) {
        return issueToken(userDetails.getUsername(), accessTokenExpiry);
    }

    public String issueRefreshToken(UserDetails userDetails) {
        return issueToken(userDetails.getUsername(), refreshTokenExpiry);
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expiration != null && expiration.after(Date.from(Instant.now()));
        } catch (ParseException | JOSEException e) {
            log.debug("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public UserDetails extractUserDetails(String token) {
        try {
            String username = SignedJWT.parse(token).getJWTClaimsSet().getSubject();
            return userDetailsService.loadUserByUsername(username);
        } catch (ParseException e) {
            throw new RuntimeException("토큰 파싱 실패", e);
        }
    }
    private String issueToken(String username, long expiryMillis) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(Instant.now()))

                    .expirationTime(Date.from(Instant.now().plusMillis(expiryMillis)))
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(signer);
            return signedJWT.serialize();
            } catch (JOSEException e) {
            throw new RuntimeException("토큰 생성 실패", e);
        }
    }
}
