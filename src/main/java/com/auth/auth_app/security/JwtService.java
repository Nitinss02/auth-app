package com.auth.auth_app.security;

import com.auth.auth_app.models.Role;
import com.auth.auth_app.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSecond;
    private final long refreshTtlSecond;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secrete,
            @Value("${security.jwt.access-ttl-second}") long accessTtlSecond,
            @Value("${security.jwt.refresh-ttl-second}") long refreshTtlSecond,
            @Value("${security.jwt.issuer}") String issuer) {

        if (secrete==null || secrete.isEmpty() || secrete.length()>64)
        {
            throw new IllegalArgumentException("invalid secret");
        }
        this.key = Keys.hmacShaKeyFor(secrete.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSecond = accessTtlSecond;
        this.refreshTtlSecond = refreshTtlSecond;
        this.issuer = issuer;
    }
    
    //genereate token
    public String generateAccessToken(User user)
    {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() : user.getRoles().stream().map(Role::getRoleName).toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSecond)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "typ", "access"
                )).signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public String refreshToken(User user, String jti)
    {
        Instant now = Instant.now();
        return  Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSecond)))
                .claim("typ", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //parse the token

    public Jws<Claims> parse(String token)
    {
        try
        {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        }catch (JwtException e)
        {
            throw e;
        }
    }

    public boolean isAccessToken(String token)
    {
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token)
    {
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token)
    {
        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    public String getJwtId(String token)
    {
        return parse(token).getPayload().getId();
    }
}
