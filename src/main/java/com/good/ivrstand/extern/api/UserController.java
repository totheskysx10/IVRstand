package com.good.ivrstand.extern.api;

import com.good.ivrstand.app.UserService;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.exception.NotConfirmedEmailException;
import com.good.ivrstand.exception.TokenException;
import com.good.ivrstand.exception.UserDuplicateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/users")
@Tag(name = "UserController", description = "Контроллер для управления пользователями")
public class UserController {

    private final UserService userService;
    private final UserAssembler userAssembler;

    public UserController(UserService userService, UserAssembler userAssembler) {
        this.userService = userService;
        this.userAssembler = userAssembler;
    }

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "409", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "204", description = "Пользователь не найден")
    })
    @PostMapping
    public ResponseEntity<UserRegisterDTO> createUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getPasswordConfirm())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            User user = User.builder()
                    .username(userRegisterDTO.getUsername())
                    .password(userRegisterDTO.getPassword())
                    .email(userRegisterDTO.getEmail())
                    .firstName(userRegisterDTO.getFirstName())
                    .lastName(userRegisterDTO.getLastName())
                    .roles(new ArrayList<>())
                    .emailConfirmed(false)
                    .resetToken("no-token")
                    .build();
            userService.createUser(user);
            return new ResponseEntity<>(userRegisterDTO, HttpStatus.CREATED);
        } catch (UserDuplicateException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Обновить пароль", description = "Обновляет пароль пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Токен не найден")
    })
    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@RequestParam String userId, @RequestParam String token, @Valid @RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        if (!userUpdatePasswordDTO.getPassword().equals(userUpdatePasswordDTO.getPasswordConfirm())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            userService.updatePassword(userId, userUpdatePasswordDTO.getPassword(), token);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (TokenException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Запросить сброс пароля", description = "Отправляет сообщение для сброса пароля.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сообщение успешно отправлено"),
            @ApiResponse(responseCode = "204", description = "Пользователь не найден")
    })
    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> sendPasswordResetMessage(@RequestParam String email) {
        try {
            userService.sendPasswordResetMessage(email);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Подтвердить email", description = "Подтверждает email пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email успешно подтвержден"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    @PutMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(@RequestParam String userId) {
        try {
            userService.confirmEmail(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Запросить подтверждение email", description = "Отправляет сообщение для подтверждения email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сообщение успешно отправлено"),
            @ApiResponse(responseCode = "204", description = "Пользователь не найден")
    })
    @PostMapping("/request-confirm-email")
    public ResponseEntity<Void> sendConfirmEmailMessage(@RequestParam String email) {
        try {
            userService.sendConfirmEmailMessage(email);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Получить пользователя по ID", description = "Получает информацию о пользователе по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "204", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(userAssembler.toModel(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по его идентификатору.")
    @ApiResponse(responseCode = "204", description = "Пользователь успешно удален")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Назначить администратора", description = "Назначает пользователя администратором.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно назначен администратором"),
            @ApiResponse(responseCode = "403", description = "Email не подтвержден")
    })
    @PutMapping("/admin/{userId}")
    public ResponseEntity<Void> giveAdminRulesToUser(@PathVariable long userId) {
        try {
            userService.giveAdminRulesToUser(userId);
            return ResponseEntity.ok().build();
        } catch (NotConfirmedEmailException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
    @Operation(summary = "Снять права администратора", description = "Снимает с пользователя права администратора.")
    @ApiResponse(responseCode = "200", description = "Права администратора успешно сняты")
    @PutMapping("/no-admin/{userId}")
    public ResponseEntity<Void> removeAdminRulesFromUser(@PathVariable long userId) {
        userService.removeAdminRulesFromUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить имя", description = "Обновляет имя пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Имя успешно обновлено"),
            @ApiResponse(responseCode = "204", description = "Пользователь не найден")
    })
    @PutMapping("/name/{userId}")
    public ResponseEntity<Void> updateFirstName(@PathVariable long userId, @RequestParam String name) {
        try {
            userService.updateFirstName(userId, name);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Обновить фамилию", description = "Обновляет фамилию пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фамилия успешно обновлена"),
            @ApiResponse(responseCode = "204", description = "Пользователь не найден")
    })
    @PutMapping("/surname/{userId}")
    public ResponseEntity<Void> updateLastName(@PathVariable long userId, @RequestParam String surname) {
        try {
            userService.updateLastName(userId, surname);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }
}
