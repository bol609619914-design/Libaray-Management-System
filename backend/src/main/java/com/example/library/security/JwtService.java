package com.example.library.security;

import com.example.library.config.LibraryProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final LibraryProperties properties;

    public JwtService(LibraryProperties properties) {
        this.properties = properties;
    }

    public String createToken(CurrentUser user) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.getJwt().getAccessTokenMinutes() * 60);
        return Jwts.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject(user.username())
                .claim("uid", user.id())
                .claim("role", user.role())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expires))
                .signWith(key())
                .compact();
    }

    public CurrentUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .requireIssuer(properties.getJwt().getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new CurrentUser(claims.get("uid", Long.class), claims.getSubject(), claims.get("role", String.class));
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }
}

