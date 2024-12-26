package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.extern.api.dto.RefreshTokenDTO;
import com.good.ivrstand.extern.api.dto.TokenDTO;
import com.good.ivrstand.extern.infrastructure.authentication.JwtService;
import com.good.ivrstand.extern.infrastructure.authentication.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/jwt")
@Tag(name = "JwtController", description = "Контроллер для валидации токенов")
public class JwtController {

    private final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Operation(summary = "Проверить токен доступа", description = "Проверяет токен доступа.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проверка произведена"),
            @ApiResponse(responseCode = "400", description = "Некорректные токены")
    })
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestBody @Valid TokenDTO tokenDTO) {
        String token = tokenDTO.getToken();
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean validationResult = jwtService.isTokenValid(token, TokenType.ACCESS_TOKEN);
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
    public ResponseEntity<Map<String, Boolean>> validateRefreshToken(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        String token = refreshTokenDTO.getRefreshToken();
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean validationResult = jwtService.isTokenValid(token, TokenType.REFRESH_TOKEN);
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
