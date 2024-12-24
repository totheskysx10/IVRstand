package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.exception.DifferentPasswordsException;
import com.good.ivrstand.exception.UserDuplicateException;
import com.good.ivrstand.exception.UserRolesException;
import com.good.ivrstand.extern.api.dto.*;
import com.good.ivrstand.extern.infrastructure.authentication.AuthService;
import com.good.ivrstand.extern.infrastructure.authentication.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "Контроллер для управления авторизацией")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "409", description = "Ошибка валидации - пользователь есть в базе и у него уже есть роль юзера"),
            @ApiResponse(responseCode = "400", description = "Пароли не совпадают"),
            @ApiResponse(responseCode = "204", description = "Пользователь не создан")
    })
    @Transactional
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            response = authService.registerUser(userRegisterDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UserDuplicateException | UserRolesException e) {
            response.put("error", "Пользователь уже существует/у него уже есть роль простого юзера");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } catch (IllegalArgumentException ex) {
            response.put("error", "Пользователь не создан");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (DifferentPasswordsException exc) {
            response.put("error", "Пароли не совпадают");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Войти в аккаунт", description = "Входит в аккаунт пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный логин"),
            @ApiResponse(responseCode = "401", description = "Ошибка входа"),
    })
    @PostMapping("/sign-in")
    public ResponseEntity<Map<String, String>> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            response = authService.loginUser(userLoginDTO.getUsername(), userLoginDTO.getPassword());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.put("error", "Введены неверные данные для входа");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Получить айди юзера по токену", description = "Получает айди юзера по токену.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получено. Вернётся в формате \"id\": 0"),
            @ApiResponse(responseCode = "403", description = "Токен истёк. Вернётся \"JWT expired!\": 403"),
            @ApiResponse(responseCode = "400", description = "Некорректные токены. Вернётся \"IncorrectToken!\": 400")
    })
    @GetMapping("/get-id")
    public ResponseEntity<Map<String, Long>> getIdFromToken(@RequestBody TokenDTO tokenDTO) {
        Map<String, Long> response = new HashMap<>();
        try {
            Long id = authService.getIdFromToken(tokenDTO.getToken());
            response.put("id", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ExpiredJwtException e) {
            response.put("JWT expired!", 403L);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } catch (MalformedJwtException e) {
            response.put("IncorrectToken", 400L);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Обновить токен доступа", description = "Обновляет токен доступа.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное обновление"),
            @ApiResponse(responseCode = "401", description = "Ошибка - токен доступа/обновления истёк"),
            @ApiResponse(responseCode = "403", description = "Ошибка валидации токенов"),
            @ApiResponse(responseCode = "400", description = "Некорректные токены")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody RefreshAndAuthTokenDTO refreshAndAuthTokenDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            String token = refreshAndAuthTokenDTO.getToken();
            String refreshToken = refreshAndAuthTokenDTO.getRefreshToken();
            response = authService.refreshToken(token, refreshToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ExpiredJwtException e) {
            response.put("error", "Токен доступа/обновления истёк");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            response.put("error", "Некорректные токены");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("error", "Ошибка валидации токенов");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "Проверить токен доступа", description = "Проверяет токен доступа.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проверка произведена"),
            @ApiResponse(responseCode = "400", description = "Некорректные токены")
    })
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestBody TokenDTO tokenDTO) {
        String token = tokenDTO.getToken();
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean validationResult = authService.validateToken(token, TokenType.ACCESS_TOKEN);
            response.put("valid", validationResult);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ExpiredJwtException | SignatureException e) {
            response.put("valid", false);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (MalformedJwtException e) {
            response.put("valid", false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Проверить токен сброса", description = "Проверяет токен сброса.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проверка произведена"),
            @ApiResponse(responseCode = "400", description = "Некорректные токены")
    })
    @PostMapping("/validate-refresh-token")
    public ResponseEntity<Map<String, Boolean>> validateRefreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        String token = refreshTokenDTO.getRefreshToken();
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean validationResult = authService.validateToken(token, TokenType.REFRESH_TOKEN);
            response.put("valid", validationResult);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ExpiredJwtException | SignatureException e) {
            response.put("valid", false);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (MalformedJwtException e) {
            response.put("valid", false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
