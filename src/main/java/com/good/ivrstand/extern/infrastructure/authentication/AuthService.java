package com.good.ivrstand.extern.infrastructure.authentication;

import com.good.ivrstand.app.service.EncodeService;
import com.good.ivrstand.app.service.UserService;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.exception.DifferentPasswordsException;
import com.good.ivrstand.exception.TokenRefreshException;
import com.good.ivrstand.exception.UserDuplicateException;
import com.good.ivrstand.extern.api.dto.UserRegisterDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для регистрации и авторизации
 */
@Slf4j
@Component
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EncodeService encodeService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager, EncodeService encodeService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.encodeService = encodeService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Регистрирует нового пользователя
     *
     * @param userRegisterDTO объект с данными регистрации
     * @return карта с токеном доступа и токеном обновления
     */
    public Map<String, String> registerUser(UserRegisterDTO userRegisterDTO) throws UserDuplicateException, DifferentPasswordsException {
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getPasswordConfirm())) {
            throw new DifferentPasswordsException("Пароли не совпадают!");
        }

        String encodedPass = bCryptPasswordEncoder.encode(userRegisterDTO.getPassword());
        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(encodedPass)
                .firstName(userRegisterDTO.getFirstName())
                .lastName(userRegisterDTO.getLastName())
                .roles(new ArrayList<>())
                .emailConfirmed(false)
                .resetToken("no-token")
                .build();
        userService.createUser(user);

        String jwt = jwtService.generateToken(user);
        String refreshJwt = jwtService.generateRefreshToken(user, encodeService.encrypt(userRegisterDTO.getPassword()));

        Map<String, String> result = new HashMap<>();
        result.put("token", jwt);
        result.put("refreshToken", refreshJwt);
        log.info("Пользователь {} зарегистрирован", userRegisterDTO.getUsername());

        return result;
    }

    /**
     * Авторизует пользователя
     *
     * @param username логин
     * @param password пароль
     * @return карта с токеном доступа и токеном обновления
     */
    public Map<String, String> loginUser(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                username,
                password
        ));

        UserDetails user = userService.userDetailsService().loadUserByUsername(username);

        String jwt = jwtService.generateToken(user);
        String refreshJwt = jwtService.generateRefreshToken(user, encodeService.encrypt(password));

        Map<String, String> result = new HashMap<>();
        result.put("token", jwt);
        result.put("refreshToken", refreshJwt);
        log.info("Пользователь {} авторизован", username);

        return result;
    }

    /**
     * Получает ID пользователя из токена авторизации
     *
     * @param token токен
     */
    public Long getIdFromToken(String token) {
        String id = jwtService.extractId(token).toString();

        return Long.parseLong(id);
    }

    /**
     * Обновляет токены для пользователя
     *
     * @param token        токен доступа
     * @param refreshToken токен сброса
     * @return карта с токеном доступа и токеном обновления
     */
    public Map<String, String> refreshToken(String token, String refreshToken) throws TokenRefreshException {
        String username = jwtService.extractUsername(token);
        String encodedPassword = jwtService.extractPassword(refreshToken).toString();
        String password = encodeService.decrypt(encodedPassword);

        boolean validationResult = jwtService.validateRefreshToken(refreshToken);

        if (validationResult)
            return loginUser(username, password);
        else
            throw new TokenRefreshException("Ошибка обновления токена");
    }

    /**
     * Проверяет, действителен ли токен
     *
     * @param token     токен
     * @param tokenType тип токена
     * @return true, если токен действителен
     */
    public boolean validateToken(String token, TokenType tokenType) {
        boolean validationResult = false;

        switch (tokenType) {
            case ACCESS_TOKEN -> validationResult = jwtService.validateToken(token);
            case REFRESH_TOKEN -> validationResult = jwtService.validateRefreshToken(token);
        }

        return validationResult;
    }
}
