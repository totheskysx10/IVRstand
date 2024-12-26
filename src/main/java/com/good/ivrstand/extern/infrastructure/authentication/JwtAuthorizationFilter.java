package com.good.ivrstand.extern.infrastructure.authentication;

import com.good.ivrstand.app.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр авторизации при помощи JWT
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthorizationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Фильтрует входящий HTTP-запрос для проверки наличия и валидации JWT-токена в заголовке Authorization.
     * Если токен действителен, выполняется аутентификация пользователя.
     * Если токен недействителен, возвращается ошибка 403 с соответствующим сообщением.
     *
     * @param request     запрос
     * @param response    ответ для отправки в случае ошибки аутентификации
     * @param filterChain цепочка фильтров для дальнейшей обработки запроса
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        String jwt = null;
        String username = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtService.extractUsername(jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails;
                try {
                    userDetails = userService
                            .userDetailsService()
                            .loadUserByUsername(username);
                } catch (UsernameNotFoundException ex) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("User with this token was deleted!");
                    response.getWriter().flush();
                    return;
                }

                if (jwtService.isTokenValidAndMatchesUser(jwt, userDetails)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("JWT expired!");
            response.getWriter().flush();
        } catch (SignatureException e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("JWT error! Use main token instead of refresh token!");
            response.getWriter().flush();
        }
    }
}