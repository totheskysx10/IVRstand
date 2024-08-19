package com.good.ivrstand.extern.infrastructure.authentication;

import com.good.ivrstand.app.EncodeService;
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

@Component
public class JwtService {

    @Value("${auth.key}")
    private String signKey;

    @Value("${auth.refresh-key}")
    private String refreshKey;

    /**
     * Генерация токена обновления
     *
     * @param userDetails данные пользователя
     * @param password пароль
     * @return токен
     */
    public String generateRefreshToken(UserDetails userDetails, String password) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("password", password);
        }
        return generateToken(claims, userDetails, refreshKey);
    }

    /**
     * Генерация токена
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
     * Проверка токена на просроченность
     *
     * @param token токен
     * @param userDetails детали пользователя
     * @return true, если токен просрочен
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, signKey));
    }

    /**
     * Проверка токена обновления на просроченность
     *
     * @param token токен
     * @param userDetails детали пользователя
     * @return true, если токен просрочен
     */
    public boolean validateRefreshToken(String token, String refreshToken, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(refreshToken, refreshKey));
    }

    /**
     * Проверка токена на просроченность
     *
     * @param token токен
     * @return true, если токен просрочен
     */
    private boolean isTokenExpired(String token, String key) {
        return extractExpiration(token, key).before(new Date());
    }

    /**
     * Извлечение имени из токена
     *
     * @param token токен
     * @return имя юзера
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject, signKey);
    }

    /**
     * Извлечение ID из токена
     *
     * @param token токен
     * @return ID пользователя
     */
    public Object extractId(String token) {
        return extractClaim(token, claims -> claims.get("id"), signKey);
    }

    /**
     * Извлечение пароля из токена
     *
     * @param token токен
     * @return пароль пользователя, шифрованный
     */
    public Object extractPassword(String token) {
        return extractClaim(token, claims -> claims.get("password"), refreshKey);
    }

    /**
     * Алгоритм генерации токена
     *
     * @param extraClaims дополнительные данные
     * @param userDetails данные пользователя
     * @return токен
     */
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String key) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 12 * 60 * 60 * 1000))
                .signWith(getSignInKey(key), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Извлечение всех данных из токена
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
     * Извлечение данных из токена
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
     * Извлечение даты истечения токена
     *
     * @param token токен
     * @return дата истечения
     */
    private Date extractExpiration(String token, String key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    /**
     * Получение ключа для подписи токена
     *
     * @return ключ
     */
    private SecretKey getSignInKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
