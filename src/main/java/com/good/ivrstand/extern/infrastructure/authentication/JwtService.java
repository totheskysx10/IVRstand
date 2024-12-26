package com.good.ivrstand.extern.infrastructure.authentication;

import com.good.ivrstand.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервис для работы с JWT
 */
@Component
public class JwtService {

    /**
     * Время, которое токен действителен (в часах)
     */
    private static final int HOURS_TO_EXPIRE = 12;
    @Value("${auth.key}")
    private String signKey;
    @Value("${auth.refresh-key}")
    private String refreshKey;

    /**
     * Генерирует токен обновления сессии.
     *
     * @param userDetails данные пользователя
     * @param password    пароль
     * @return токен
     */
    public String generateRefreshToken(UserDetails userDetails, String password) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User) {
            claims.put("password", password);
        }
        return generateToken(claims, userDetails, refreshKey);
    }

    /**
     * Генерирует токен доступа.
     *
     * @param userDetails данные пользователя
     * @return токен
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            Long id = customUserDetails.getId();
            claims.put("id", id);
            claims.put("roles", customUserDetails.getRoles());
        }
        return generateToken(claims, userDetails, signKey);
    }

    /**
     * Проверяет токен на просроченность,
     * проверяет его соответствие пользователю.
     *
     * @param token       токен
     * @param userDetails детали пользователя
     * @return true, если токен годен
     */
    public boolean isTokenValidAndMatchesUser(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && isTokenValid(token, TokenType.ACCESS_TOKEN));
    }

    /**
     * Проверяет валидность токена.
     *
     * @param token токен
     * @param tokenType тип токена
     * @return true, если токен годен
     */
    public boolean isTokenValid(String token, TokenType tokenType) {
        boolean validationResult = false;

        switch (tokenType) {
            case ACCESS_TOKEN -> validationResult = !(extractExpiration(token, signKey).before(new Date()));
            case REFRESH_TOKEN -> validationResult = !(extractExpiration(token, refreshKey).before(new Date()));
        }

        return validationResult;
    }

    /**
     * Извлекает имя пользователя из токена.
     *
     * @param token токен
     * @return имя юзера
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject, signKey);
    }

    /**
     * Извлекает ID пользователя из токена доступа.
     *
     * @param token токен
     * @return ID пользователя
     */
    public Object extractId(String token) {
        return extractClaim(token, claims -> claims.get("id"), signKey);
    }

    /**
     * Извлекает пароль из токена обновления.
     *
     * @param token токен
     * @return пароль пользователя, шифрованный
     */
    public Object extractPassword(String token) {
        return extractClaim(token, claims -> claims.get("password"), refreshKey);
    }

    /**
     * Генерирует JWT-токен.
     *
     * @param extraClaims дополнительные данные
     * @param userDetails данные пользователя
     * @return токен
     */
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String key) {
        long millisToExpire = HOURS_TO_EXPIRE * 60 * 60 * 1000;
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + millisToExpire))
                .signWith(getSignInKey(key), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Извлекает все данные из токена.
     *
     * @param token токен
     * @return данные
     */
    private Claims getAllClaimsFromToken(String token, String key) {
        return Jwts.parser()
                .verifyWith(getSignInKey(key))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Извлекает данные из токена (общий метод).
     *
     * @param token           токен
     * @param claimsResolvers функция извлечения данных
     * @param <T>             тип данных
     * @return данные
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers, String key) {
        final Claims claims = getAllClaimsFromToken(token, key);
        return claimsResolvers.apply(claims);
    }

    /**
     * Извлекает дату истечения токена
     *
     * @param token токен
     * @return дата истечения
     */
    private Date extractExpiration(String token, String key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    /**
     * Получает ключ для подписи токена
     */
    private SecretKey getSignInKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
