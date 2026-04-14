package com.taskflow.backend.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationHours;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-hours}") long expirationHours
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    public String generateToken(CurrentUser currentUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(currentUser.id().toString())
                .claim("user_id", currentUser.id().toString())
                .claim("email", currentUser.email())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    public boolean isTokenValid(String token, CurrentUser currentUser) {
        Claims claims = extractClaims(token);
        String subject = claims.getSubject();
        Date expiration = claims.getExpiration();
        return currentUser.id().toString().equals(subject) && expiration.after(new Date());
    }
}
