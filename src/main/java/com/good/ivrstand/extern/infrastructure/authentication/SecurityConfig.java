package com.good.ivrstand.extern.infrastructure.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Конфигурация безопасности
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final SecurityContextService securityContextService;

    public SecurityConfig(JwtAuthorizationFilter jwtAuthorizationFilter, SecurityContextService securityContextService) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.securityContextService = securityContextService;
    }

    /**
     * Возвращает объект менеджера аутентификации.
     *
     * @param config конфигурация
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Конфигурирует параметры доступа к эндпойнтам.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.GET, "/items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/additions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/notifications/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/users/delete/{userId}", "/users/name/{userId}", "/users/surname/{userId}")
                        .access((authentication, context) -> {
                            long userId = Long.parseLong(context.getVariables().get("userId"));
                            return new AuthorizationDecision(securityContextService.isCurrentAuthId(userId));
                        })
                        .requestMatchers(HttpMethod.GET, "/users/**").authenticated()
                        //.requestMatchers("/users/admin/**", "/users/no-admin/**").hasRole("ADMIN") // TODO lock
                        .requestMatchers("/users/**", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable);

        SecurityFilterChain filterChain = http.build();
        log.info("Security filter chain configured successfully");

        return filterChain;
    }
}
