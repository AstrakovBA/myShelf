package com.myshelf.wardrobe.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Компонент для генерации и валидации JWT токенов.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    /**
     * Инициализирует провайдер с секретом и сроком жизни токена.
     *
     * @param secretKeyString секрет для подписи HS256
     * @param validityInMilliseconds время жизни токена в миллисекундах
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.expiration-ms:86400000}") long validityInMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /**
     * Генерирует JWT для указанного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return подписанный JWT
     */
    public String generateToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлекает идентификатор пользователя из JWT.
     *
     * @param token JWT
     * @return UUID пользователя из subject токена
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Проверяет подпись и срок действия токена.
     *
     * @param token JWT
     * @return {@code true}, если токен валиден и не истёк
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
