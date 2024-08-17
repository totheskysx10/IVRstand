package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.UserService;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.exception.UserDuplicateException;
import com.good.ivrstand.extern.infrastructure.authentication.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "Контроллер для управления авторизацией")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "409", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "204", description = "Пользователь не создан")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<JwtDTO> createUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getPasswordConfirm())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            User user = User.builder()
                    .username(userRegisterDTO.getUsername())
                    .password(userRegisterDTO.getPassword())
                    .email(userRegisterDTO.getUsername())
                    .firstName(userRegisterDTO.getFirstName())
                    .lastName(userRegisterDTO.getLastName())
                    .roles(new ArrayList<>())
                    .emailConfirmed(false)
                    .resetToken("no-token")
                    .build();
            userService.createUser(user);

            String jwt = jwtService.generateToken(user);
            JwtDTO jwtDTO = new JwtDTO(jwt);
            return new ResponseEntity<>(jwtDTO, HttpStatus.CREATED);
        } catch (UserDuplicateException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Войти в аккаунт", description = "Входит в аккаунт пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный логин"),
            @ApiResponse(responseCode = "401", description = "Ошибка входа"),
            @ApiResponse(responseCode = "204", description = "Такого юзера по айди из токена нет")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<JwtDTO> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userLoginDTO.getUsername(),
                    userLoginDTO.getPassword()
            ));

            UserDetails user = userService.userDetailsService().loadUserByUsername(userLoginDTO.getUsername());

            String jwt = jwtService.generateToken(user);
            JwtDTO jwtDTO = new JwtDTO(jwt);
            return new ResponseEntity<>(jwtDTO, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Получить айди юзера по токену", description = "Получает айди юзера по токену.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получено"),
            @ApiResponse(responseCode = "204", description = "Такого юзера по айди из токена нет")
    })
    @GetMapping("/get-id")
    public ResponseEntity<Map<String, Long>> getIdFromToken(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        Object id = jwtService.extractId(token);

        Long idLong;
        if (id instanceof Long)
            idLong = (Long) id;
        else if (id instanceof Integer) {
            idLong = ((Integer) id).longValue();
        } else {
            throw new IllegalArgumentException("Unexpected type for id: " + id.getClass().getName());
        }

        Map<String, Long> response = new HashMap<>();
        response.put("id", idLong);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
