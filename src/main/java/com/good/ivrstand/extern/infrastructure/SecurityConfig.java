package com.good.ivrstand.extern.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final SecurityContextService securityContextService;

    public SecurityConfig(SecurityContextService securityContextService) {
        this.securityContextService = securityContextService;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.GET, "/items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/additions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/notifications/**").permitAll()
                        .requestMatchers("/users/delete/{userId}", "/users/name/{userId}", "/users/surname/{userId}")
                        .access((authentication, context) -> {
                            long userId = Long.parseLong(context.getVariables().get("userId"));
                            return new AuthorizationDecision(securityContextService.isCurrentAuthId(userId));
                        })
                        .requestMatchers(HttpMethod.GET, "/users/**").authenticated()
                        .requestMatchers("/users/**", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                        //.anyRequest().hasRole("ADMIN") //TODO
                        .anyRequest().permitAll()
                )
                .formLogin((form) -> form
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .permitAll()
                )
                .csrf((csrf) -> csrf.disable());

        SecurityFilterChain filterChain = http.build();
        log.info("Security filter chain configured successfully");

        return filterChain;
    }
}
